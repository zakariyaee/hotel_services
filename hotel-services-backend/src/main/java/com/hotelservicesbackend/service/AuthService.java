package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.auth.GuestAccessRequest;
import com.hotelservicesbackend.dto.auth.GuestAccessResponse;
import com.hotelservicesbackend.dto.auth.LoginRequest;
import com.hotelservicesbackend.dto.auth.LoginResponse;
import com.hotelservicesbackend.entity.Chambre;
import com.hotelservicesbackend.entity.Client;
import com.hotelservicesbackend.entity.ClientAccessToken;
import com.hotelservicesbackend.entity.Departement;
import com.hotelservicesbackend.entity.Employe;
import com.hotelservicesbackend.entity.Reservation;
import com.hotelservicesbackend.entity.Utilisateur;
import com.hotelservicesbackend.entity.enums.StatutReservation;
import com.hotelservicesbackend.repository.ClientAccessTokenRepository;
import com.hotelservicesbackend.repository.EmployeRepository;
import com.hotelservicesbackend.repository.UtilisateurRepository;
import com.hotelservicesbackend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final ClientAccessTokenRepository clientAccessTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final EmployeRepository employeRepository;

    public AuthService(
            UtilisateurRepository utilisateurRepository,
            ClientAccessTokenRepository clientAccessTokenRepository,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService,
            EmployeRepository employeRepository
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.clientAccessTokenRepository = clientAccessTokenRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.employeRepository = employeRepository;
    }

    public LoginResponse login(LoginRequest request) {
        // Authentification via Spring Security (BCrypt inclus)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse())
        );

        // Récupération de l'utilisateur depuis la base
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Chargement des UserDetails pour générer le token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        // Génération du JWT avec userId et role en claims
        String token = jwtUtil.generateToken(userDetails, utilisateur.getIdUtilisateur(), utilisateur.getRole().name());

        return new LoginResponse(
                utilisateur.getIdUtilisateur(),
                utilisateur.getEmail(),
                utilisateur.getRole().name(),
                token,
                employeRepository.findById(utilisateur.getIdUtilisateur())
                        .map(Employe::getDepartement)
                        .map(Departement::getIdDepartement)
                        .orElse(null),
                employeRepository.findById(utilisateur.getIdUtilisateur())
                        .map(Employe::getDepartement)
                        .map(Departement::getNom)
                        .orElse(null)
        );
    }

    @Transactional
    public GuestAccessResponse guestLogin(GuestAccessRequest request) {
        String code = request.accessCode().trim();

        ClientAccessToken accessToken = clientAccessTokenRepository.findByTokenHash(code)
                .orElseThrow(() -> new IllegalArgumentException("Code d'accès introuvable. Veuillez vérifier votre code."));

        if (accessToken.isRevoked()) {
            throw new IllegalStateException("Ce code d'accès a été révoqué.");
        }

        if (accessToken.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Ce code d'accès a expiré.");
        }

        Reservation reservation = accessToken.getReservation();
        if (reservation == null) {
            throw new IllegalStateException("Aucune réservation associée à ce code.");
        }

        LocalDate today = LocalDate.now();
        if (reservation.getDateDebut() != null && today.isBefore(reservation.getDateDebut())) {
            throw new IllegalStateException("Votre séjour n'a pas encore commencé (début prévu le " + reservation.getDateDebut() + ").");
        }

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new IllegalStateException("Cette réservation a été annulée.");
        }

        if (reservation.getStatut() == StatutReservation.TERMINEE) {
            throw new IllegalStateException("Votre séjour est terminé (fin le " + reservation.getDateFin() + "). Le code n'est plus actif.");
        }

        if (reservation.getDateFin() != null && today.isAfter(reservation.getDateFin()) && reservation.getStatut() != StatutReservation.EN_COURS) {
            throw new IllegalStateException("Votre séjour est terminé (fin le " + reservation.getDateFin() + "). Le code n'est plus actif.");
        }

        // Incrémentation du compteur d'utilisations
        accessToken.setNbrUtilisations(accessToken.getNbrUtilisations() + 1);
        clientAccessTokenRepository.save(accessToken);

        Client client = reservation.getClient();
        Chambre chambre = reservation.getChambre();

        UserDetails userDetails = userDetailsService.loadUserByUsername(client.getEmail());
        String token = jwtUtil.generateGuestToken(
                userDetails,
                client.getIdUtilisateur(),
                reservation.getIdReservation(),
                chambre != null ? chambre.getNumChambre() : "N/A"
        );

        return new GuestAccessResponse(
                client.getIdUtilisateur(),
                client.getEmail(),
                client.getNom(),
                client.getPrenom(),
                "CLIENT",
                token,
                reservation.getIdReservation(),
                chambre != null ? chambre.getNumChambre() : "N/A",
                chambre != null ? chambre.getTypeChambre() : "Chambre Standard",
                reservation.getDateDebut(),
                reservation.getDateFin()
        );
    }
}
