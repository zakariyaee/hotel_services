package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.DemandeRequest;
import com.hotelservicesbackend.dto.DemandeUpdateRequest;
import com.hotelservicesbackend.entity.*;
import com.hotelservicesbackend.entity.enums.PrioriteDemande;
import com.hotelservicesbackend.entity.enums.StatutDemande;
import com.hotelservicesbackend.entity.enums.StatutReservation;
import com.hotelservicesbackend.repository.ClientRepository;
import com.hotelservicesbackend.repository.DemandeRepository;
import com.hotelservicesbackend.repository.ReservationRepository;
import com.hotelservicesbackend.repository.ServiceRepository;
import com.hotelservicesbackend.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@org.springframework.stereotype.Service
public class DemandeService {
    private final DemandeRepository demandeRepository;
    private final ServiceRepository serviceRepository;
    private final ClientRepository clientRepository;
    private final ReservationRepository reservationRepository;
    private final RealtimeNotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;
    private final HistoriqueDemandeService historiqueDemandeService;

    public DemandeService(DemandeRepository demandeRepository, ServiceRepository serviceRepository,
                          ClientRepository clientRepository, ReservationRepository reservationRepository,
                          RealtimeNotificationService notificationService, UtilisateurRepository utilisateurRepository,
                          HistoriqueDemandeService historiqueDemandeService) {
        this.demandeRepository = demandeRepository;
        this.serviceRepository = serviceRepository;
        this.clientRepository = clientRepository;
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
        this.utilisateurRepository = utilisateurRepository;
        this.historiqueDemandeService = historiqueDemandeService;
    }

    public List<Demande> findAll() { return demandeRepository.findAll(); }

    public List<Demande> findByClient(Long clientId, String requesterEmail) {
        if (clientId == null) {
            return Collections.emptyList();
        }
        assertCanAccessClientData(clientId, requesterEmail);
        return demandeRepository.findByClient_IdUtilisateur(clientId);
    }

    public Demande findById(Long id) {
        return demandeRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable"));
    }

    public Demande findByIdForRequester(Long id, String requesterEmail) {
        Demande demande = findById(id);
        assertCanAccessClientData(demande.getClient().getIdUtilisateur(), requesterEmail);
        return demande;
    }

    public Demande create(DemandeRequest request, String requesterEmail) {
        Long serviceId = request.getServiceId();
        if (serviceId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identifiant de service obligatoire");
        }
        Service service = serviceRepository.findById(serviceId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Service introuvable: " + serviceId));
        if (!service.isDisponibilite()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce service est désactivé et ne peut plus recevoir de demande.");
        }
        if (service.getDepartement() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce service n'est rattaché à aucun département.");
        }

        Long clientId = request.getClientId();
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identifiant client obligatoire");
        }
        Client client = clientRepository.findById(clientId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable: " + clientId));
        assertCanCreateForClient(clientId, requesterEmail);

        // CDC01 & REQ-RES-ACT : Vérification stricte de l'éligibilité du séjour (réservation active)
        LocalDate today = LocalDate.now();
        List<Reservation> clientReservations = reservationRepository.findByClient_IdUtilisateur(clientId);
        boolean hasActiveStay = clientReservations.stream().anyMatch(r ->
                (r.getStatut() == StatutReservation.EN_COURS || r.getStatut() == StatutReservation.CONFIRMEE)
                && (r.getDateDebut() == null || !today.isBefore(r.getDateDebut()))
                && (r.getDateFin() == null || !today.isAfter(r.getDateFin()))
        );

        if (!hasActiveStay) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Action refusée : aucune réservation active trouvée à la date d'aujourd'hui pour ce client.");
        }

        Demande demande = new Demande();
        demande.setService(service);
        demande.setClient(client);
        demande.setDateCreation(LocalDateTime.now());
        demande.setStatut(StatutDemande.EN_ATTENTE);
        demande.setPriorite(request.getPriorite() == null ? PrioriteDemande.NORMALE : request.getPriorite());
        demande.setTempsTraitementEstime(request.getTempsTraitementEstime() == null
                ? service.getDureeEstimee() : request.getTempsTraitementEstime());

        String ref = request.getReferenceDemande();
        if (ref == null || ref.isBlank()) {
            ref = "DEM-" + System.currentTimeMillis();
        }
        if (demandeRepository.findByReferenceDemande(ref).isPresent()) {
            ref = ref + "-" + (int) (Math.random() * 900 + 100);
        }
        demande.setReferenceDemande(ref);
        if (request.getCommentairesClient() != null && !request.getCommentairesClient().isBlank()) {
            demande.setCommentairesClient(request.getCommentairesClient().trim());
        }

        Demande saved = demandeRepository.save(demande);
        historiqueDemandeService.record(saved, null, saved.getStatut(), requesterEmail, "Demande créée");
        try {
            notificationService.notifyNewDemande(saved);
        } catch (Exception e) {
            // Ne bloque pas la création en cas d'erreur de notification
        }
        return saved;
    }

    public Demande update(Long id, DemandeUpdateRequest request, String requesterEmail) {
        Demande demande = findById(id);
        assertCanUpdate(demande, request, requesterEmail);
        StatutDemande ancienStatut = demande.getStatut();
        if (request.priorite() != null) demande.setPriorite(request.priorite());
        if (request.statut() != null) demande.setStatut(request.statut());
        if (request.motifRejet() != null) demande.setMotifRejet(request.motifRejet());
        if (request.motifAnnulation() != null) demande.setMotifAnnulation(request.motifAnnulation());
        if (request.tempsTraitementReel() != null) demande.setTempsTraitementReel(request.tempsTraitementReel());
        demande.setDateModification(LocalDateTime.now());
        Demande saved = demandeRepository.save(demande);
        historiqueDemandeService.record(saved, ancienStatut, saved.getStatut(), requesterEmail,
                request.motifAnnulation() != null ? request.motifAnnulation() : null);

        try {
            if (request.statut() == StatutDemande.REJETEE) {
                notificationService.notifyTaskRejected(saved, request.motifRejet());
            } else if (request.statut() == StatutDemande.ANNULEE) {
                notificationService.notifyTaskCancelled(saved, request.motifAnnulation());
            }
        } catch (Exception e) {
            // Ne bloque pas la mise à jour
        }
        return saved;
    }

    public void delete(Long id) { demandeRepository.delete(findById(id)); }

    private Utilisateur getRequester(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié"));
    }

    private boolean isStaff(Utilisateur user) {
        return user.getRole() != null && switch (user.getRole()) {
            case ADMIN, PERSONNEL, STAFF, EMPLOYE, RECEPTIONNISTE -> true;
            default -> false;
        };
    }

    private void assertCanAccessClientData(Long clientId, String requesterEmail) {
        Utilisateur requester = getRequester(requesterEmail);
        if (!isStaff(requester) && !requester.getIdUtilisateur().equals(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez consulter que vos propres demandes.");
        }
    }

    private void assertCanCreateForClient(Long clientId, String requesterEmail) {
        Utilisateur requester = getRequester(requesterEmail);
        if (!isStaff(requester) && !requester.getIdUtilisateur().equals(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez créer une demande que pour votre propre compte.");
        }
    }

    private void assertCanUpdate(Demande demande, DemandeUpdateRequest request, String requesterEmail) {
        Utilisateur requester = getRequester(requesterEmail);
        if (isStaff(requester)) return;
        if (!requester.getIdUtilisateur().equals(demande.getClient().getIdUtilisateur())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez modifier que vos propres demandes.");
        }
        if (request.statut() != StatutDemande.ANNULEE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le client peut uniquement annuler sa demande.");
        }
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette demande a déjà été prise en charge et ne peut plus être annulée.");
        }
    }
}
