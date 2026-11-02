package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.admin.AdminReservationResponse;
import com.hotelservicesbackend.dto.admin.CreateReservationRequest;
import com.hotelservicesbackend.entity.Chambre;
import com.hotelservicesbackend.entity.Client;
import com.hotelservicesbackend.entity.ClientAccessToken;
import com.hotelservicesbackend.entity.Reservation;
import com.hotelservicesbackend.entity.enums.Role;
import com.hotelservicesbackend.entity.enums.StatutChambre;
import com.hotelservicesbackend.entity.enums.StatutReservation;
import com.hotelservicesbackend.repository.ChambreRepository;
import com.hotelservicesbackend.repository.ClientAccessTokenRepository;
import com.hotelservicesbackend.repository.ClientRepository;
import com.hotelservicesbackend.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final ChambreRepository chambreRepository;
    private final ClientAccessTokenRepository clientAccessTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final QrCodeService qrCodeService;
    private final EmailNotificationService emailNotificationService;

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    public AdminReservationService(
            ReservationRepository reservationRepository,
            ClientRepository clientRepository,
            ChambreRepository chambreRepository,
            ClientAccessTokenRepository clientAccessTokenRepository,
            PasswordEncoder passwordEncoder,
            QrCodeService qrCodeService,
            EmailNotificationService emailNotificationService
    ) {
        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
        this.chambreRepository = chambreRepository;
        this.clientAccessTokenRepository = clientAccessTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.qrCodeService = qrCodeService;
        this.emailNotificationService = emailNotificationService;
    }

    public List<AdminReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AdminReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable"));
        return mapToResponse(reservation);
    }

    @Transactional
    public AdminReservationResponse createReservation(CreateReservationRequest request) {
        // 1. Validation de la chambre
        Chambre chambre = chambreRepository.findById(request.idChambre())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chambre introuvable"));

        // 2. Recherche ou Création du Client
        String email = request.clientEmail().trim().toLowerCase();
        String generatedCode = generateAccessCode();

        Client client = clientRepository.findByEmail(email).orElseGet(() -> {
            Client newClient = new Client();
            newClient.setEmail(email);
            newClient.setRole(Role.CLIENT);
            newClient.setLanguePreferee("FR");
            newClient.setPreferenceNotification(true);
            return newClient;
        });

        client.setNom(request.clientNom().trim());
        client.setPrenom(request.clientPrenom().trim());
        if (request.clientTel() != null && !request.clientTel().isBlank()) {
            client.setTel(request.clientTel().trim());
        }
        // Le code de réservation devient le mot de passe du client pour le login unifié
        client.setMotDePasse(passwordEncoder.encode(generatedCode));
        client.setReservationStatut(true);
        Client savedClient = clientRepository.save(client);

        // 3. Création de la réservation
        Reservation reservation = new Reservation();
        reservation.setClient(savedClient);
        reservation.setChambre(chambre);
        reservation.setDateDebut(request.dateDebut());
        reservation.setDateFin(request.dateFin());
        reservation.setStatut(StatutReservation.EN_COURS);
        reservation.setTypeReservation(request.typeReservation() != null && !request.typeReservation().isBlank()
                ? request.typeReservation().trim()
                : "Séjour Hôtelier Standard");

        Reservation savedReservation = reservationRepository.save(reservation);

        // 4. Génération du token d'accès client
        ClientAccessToken token = new ClientAccessToken();
        token.setTokenHash(generatedCode);
        token.setDateCreation(LocalDateTime.now());
        token.setDateExpiration(request.dateFin().atTime(23, 59, 59));
        token.setRevoked(false);
        token.setNbrUtilisations(0);
        token.setReservation(savedReservation);
        clientAccessTokenRepository.save(token);

        // 5. Mise à jour statut de la chambre
        chambre.setStatut(StatutChambre.OCCUPEE);
        chambreRepository.save(chambre);

        // 6. URL d'auto-login et QR Code
        String loginUrl = buildAutoLoginUrl(savedClient.getEmail(), generatedCode);
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(loginUrl, 320, 320);

        // 7. Envoi de l'email de confirmation au client
        emailNotificationService.sendReservationWelcomeEmail(
                savedClient.getEmail(),
                savedClient.getPrenom() + " " + savedClient.getNom(),
                chambre.getNumChambre(),
                chambre.getTypeChambre(),
                savedReservation.getDateDebut(),
                savedReservation.getDateFin(),
                generatedCode,
                loginUrl,
                qrCodeBase64
        );

        return new AdminReservationResponse(
                savedReservation.getIdReservation(),
                savedReservation.getDateDebut(),
                savedReservation.getDateFin(),
                savedReservation.getStatut().name(),
                savedReservation.getTypeReservation(),
                savedClient.getIdUtilisateur(),
                savedClient.getNom(),
                savedClient.getPrenom(),
                savedClient.getEmail(),
                savedClient.getTel(),
                chambre.getIdChambre(),
                chambre.getNumChambre(),
                chambre.getTypeChambre(),
                generatedCode,
                qrCodeBase64,
                loginUrl
        );
    }

    private AdminReservationResponse mapToResponse(Reservation r) {
        String accessCode = clientAccessTokenRepository
                .findTopByReservation_IdReservationOrderByDateCreationDesc(r.getIdReservation())
                .map(ClientAccessToken::getTokenHash)
                .orElse("RES-" + r.getIdReservation());

        String loginUrl = buildAutoLoginUrl(r.getClient().getEmail(), accessCode);
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(loginUrl, 260, 260);

        return new AdminReservationResponse(
                r.getIdReservation(),
                r.getDateDebut(),
                r.getDateFin(),
                r.getStatut() != null ? r.getStatut().name() : "EN_COURS",
                r.getTypeReservation(),
                r.getClient() != null ? r.getClient().getIdUtilisateur() : null,
                r.getClient() != null ? r.getClient().getNom() : "",
                r.getClient() != null ? r.getClient().getPrenom() : "",
                r.getClient() != null ? r.getClient().getEmail() : "",
                r.getClient() != null ? r.getClient().getTel() : "",
                r.getChambre() != null ? r.getChambre().getIdChambre() : null,
                r.getChambre() != null ? r.getChambre().getNumChambre() : "N/A",
                r.getChambre() != null ? r.getChambre().getTypeChambre() : "Chambre Standard",
                accessCode,
                qrCodeBase64,
                loginUrl
        );
    }

    private String buildAutoLoginUrl(String email, String code) {
        return "http://localhost:4200/login?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&auto=true";
    }

    private String generateAccessCode() {
        StringBuilder sb = new StringBuilder("RES-");
        for (int i = 0; i < 6; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
