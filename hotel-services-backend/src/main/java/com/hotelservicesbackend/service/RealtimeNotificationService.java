package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.notification.WebSocketNotificationEvent;
import com.hotelservicesbackend.entity.*;
import com.hotelservicesbackend.entity.enums.Role;
import com.hotelservicesbackend.entity.enums.StatutNotification;
import com.hotelservicesbackend.repository.*;
import com.hotelservicesbackend.websocket.NotificationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RealtimeNotificationService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeNotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    public RealtimeNotificationService(
            NotificationRepository notificationRepository,
            UtilisateurRepository utilisateurRepository,
            EmployeRepository employeRepository,
            ReservationRepository reservationRepository,
            NotificationWebSocketHandler webSocketHandler
    ) {
        this.notificationRepository = notificationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
        this.reservationRepository = reservationRepository;
        this.webSocketHandler = webSocketHandler;
    }

    private String getChambreNum(Demande d) {
        if (d != null && d.getClient() != null) {
            List<Reservation> resList = reservationRepository.findByClient_IdUtilisateur(d.getClient().getIdUtilisateur());
            if (!resList.isEmpty() && resList.get(0).getChambre() != null) {
                return resList.get(0).getChambre().getNumChambre();
            }
        }
        return "201";
    }

    private Notification saveNotification(Utilisateur u, Demande d, String titre, String message) {
        Notification n = new Notification();
        n.setUtilisateur(u);
        n.setDemande(d);
        n.setTitre(titre);
        n.setMessage(message);
        n.setDateCreation(LocalDateTime.now());
        n.setDateEnvoi(LocalDateTime.now());
        n.setStatutEnvoi(StatutNotification.ENVOYEE);
        return notificationRepository.save(n);
    }

    private WebSocketNotificationEvent buildEvent(
            Long notifId,
            String type,
            String titre,
            String message,
            Demande d
    ) {
        String numChambre = getChambreNum(d);
        com.hotelservicesbackend.entity.Service s = d != null ? d.getService() : null;
        Departement dept = s != null ? s.getDepartement() : null;
        Client c = d != null ? d.getClient() : null;

        return new WebSocketNotificationEvent(
                notifId,
                type,
                titre,
                message,
                d != null ? d.getIdDemande() : null,
                d != null ? d.getReferenceDemande() : "",
                s != null ? s.getIdService() : null,
                s != null ? s.getNom() : "",
                dept != null ? dept.getIdDepartement() : null,
                dept != null ? dept.getNom() : "Général",
                c != null ? c.getIdUtilisateur() : null,
                c != null ? (c.getPrenom() + " " + c.getNom()) : "",
                numChambre,
                d != null && d.getPriorite() != null ? d.getPriorite().name() : "NORMALE",
                d != null && d.getStatut() != null ? d.getStatut().name() : "EN_ATTENTE",
                LocalDateTime.now()
        );
    }

    // =========================================================================
    // 1. ÉVÉNEMENT : NOUVELLE DEMANDE (Client ou Réception)
    // =========================================================================
    @Transactional
    public void notifyNewDemande(Demande demande) {
        String numChambre = getChambreNum(demande);
        String serviceNom = demande.getService() != null ? demande.getService().getNom() : "Service";
        Departement dept = demande.getService() != null ? demande.getService().getDepartement() : null;
        Long deptId = dept != null ? dept.getIdDepartement() : null;
        String deptNom = dept != null ? dept.getNom() : "Général";

        String titreStaff = "Nouvelle demande : " + serviceNom;
        String msgStaff = "Chambre " + numChambre + " · Priorité " + demande.getPriorite() + " [" + demande.getReferenceDemande() + "]";

        // 1.1 Persistance et envoi pour les employés du département
        if (deptId != null) {
            List<Employe> team = employeRepository.findByDepartementIdDepartement(deptId);
            for (Employe emp : team) {
                saveNotification(emp, demande, titreStaff, msgStaff);
            }
        }

        // 1.2 Persistance pour la Réception et Admin
        List<Utilisateur> receptionnistesEtAdmins = utilisateurRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.RECEPTIONNISTE || u.getRole() == Role.ADMIN)
                .toList();
        for (Utilisateur u : receptionnistesEtAdmins) {
            saveNotification(u, demande, "Nouvelle demande [" + deptNom + "]", "Ch. " + numChambre + " - " + serviceNom);
        }

        // 1.3 Persistance pour le Client demandeur
        if (demande.getClient() != null) {
            saveNotification(
                    demande.getClient(),
                    demande,
                    "Demande confirmée (" + demande.getReferenceDemande() + ")",
                    "Votre demande de " + serviceNom + " a bien été transmise à notre équipe " + deptNom + "."
            );
        }

        // 1.4 Broadcast WebSocket en temps réel
        WebSocketNotificationEvent event = buildEvent(null, "NOUVELLE_DEMANDE", titreStaff, msgStaff, demande);

        if (deptId != null) {
            webSocketHandler.sendToDepartment(deptId, event);
        }
        webSocketHandler.sendToReceptionAndAdmin(event);

        if (demande.getClient() != null) {
            WebSocketNotificationEvent clientEvent = buildEvent(
                    null,
                    "DEMANDE_CREEE_CLIENT",
                    "Demande confirmée",
                    "Votre demande " + demande.getReferenceDemande() + " est enregistrée.",
                    demande
            );
            webSocketHandler.sendToUser(demande.getClient().getIdUtilisateur(), clientEvent);
        }

        log.info("🔔 Notification temps réel émise pour nouvelle demande {}", demande.getReferenceDemande());
    }

    // =========================================================================
    // 2. ÉVÉNEMENT : PRISE EN CHARGE D'UNE TÂCHE PAR UN EMPLOYÉ
    // =========================================================================
    @Transactional
    public void notifyTaskClaimed(Demande demande, Employe employe) {
        String empName = employe != null ? (employe.getPrenom() + " " + employe.getNom()) : "Un agent";
        String serviceNom = demande.getService() != null ? demande.getService().getNom() : "Service";
        String numChambre = getChambreNum(demande);

        // Notifier le Client
        if (demande.getClient() != null) {
            String titre = "Prise en charge : " + serviceNom;
            String msg = empName + " a pris en charge votre demande. Il arrive dans votre chambre " + numChambre + ".";
            saveNotification(demande.getClient(), demande, titre, msg);

            WebSocketNotificationEvent clientEvent = buildEvent(null, "TACHE_PRISE_EN_CHARGE", titre, msg, demande);
            webSocketHandler.sendToUser(demande.getClient().getIdUtilisateur(), clientEvent);
        }

        // Notifier le Superviseur et la Réception
        String titreStaff = "Tâche prise en charge";
        String msgStaff = empName + " a pris en charge " + demande.getReferenceDemande() + " (Ch. " + numChambre + ").";
        WebSocketNotificationEvent staffEvent = buildEvent(null, "TACHE_PRISE_EN_CHARGE_STAFF", titreStaff, msgStaff, demande);

        if (demande.getService() != null && demande.getService().getDepartement() != null) {
            webSocketHandler.sendToDepartment(demande.getService().getDepartement().getIdDepartement(), staffEvent);
        }
        webSocketHandler.sendToReceptionAndAdmin(staffEvent);
    }

    // =========================================================================
    // 3. ÉVÉNEMENT : AFFECTATION PAR UN SUPERVISEUR
    // =========================================================================
    @Transactional
    public void notifyTaskAssigned(Demande demande, Employe assignee, Utilisateur supervisor) {
        String supervisorName = supervisor != null ? (supervisor.getPrenom() + " " + supervisor.getNom()) : "Votre superviseur";
        String assigneeName = assignee != null ? (assignee.getPrenom() + " " + assignee.getNom()) : "Un employé";
        String serviceNom = demande.getService() != null ? demande.getService().getNom() : "Service";
        String numChambre = getChambreNum(demande);

        // 3.1 Notifier l'employé assigné
        if (assignee != null) {
            String titreEmp = "Nouvelle tâche assignée !";
            String msgEmp = supervisorName + " vous a assigné " + serviceNom + " (Chambre " + numChambre + ").";
            saveNotification(assignee, demande, titreEmp, msgEmp);

            WebSocketNotificationEvent empEvent = buildEvent(null, "TACHE_ASSIGNEE_EMPLOYE", titreEmp, msgEmp, demande);
            webSocketHandler.sendToUser(assignee.getIdUtilisateur(), empEvent);
        }

        // 3.2 Notifier le Client
        if (demande.getClient() != null) {
            String titreClient = "Demande assignée : " + serviceNom;
            String msgClient = assigneeName + " a été affecté(e) à votre demande et débute le traitement.";
            saveNotification(demande.getClient(), demande, titreClient, msgClient);

            WebSocketNotificationEvent clientEvent = buildEvent(null, "TACHE_ASSIGNEE_CLIENT", titreClient, msgClient, demande);
            webSocketHandler.sendToUser(demande.getClient().getIdUtilisateur(), clientEvent);
        }

        // 3.3 Notifier Réception
        WebSocketNotificationEvent recEvent = buildEvent(
                null,
                "TACHE_ASSIGNEE_RECEPTION",
                "Tâche affectée",
                demande.getReferenceDemande() + " affectée à " + assigneeName,
                demande
        );
        webSocketHandler.sendToReceptionAndAdmin(recEvent);
    }

    // =========================================================================
    // 4. ÉVÉNEMENT : TÂCHE TERMINÉE
    // =========================================================================
    @Transactional
    public void notifyTaskCompleted(Demande demande, Employe employe) {
        String serviceNom = demande.getService() != null ? demande.getService().getNom() : "Service";
        String numChambre = getChambreNum(demande);
        int tempsReel = demande.getTempsTraitementReel() != null ? demande.getTempsTraitementReel() : 0;

        // Notifier le Client
        if (demande.getClient() != null) {
            String titre = "Service terminé ✓ (" + serviceNom + ")";
            String msg = "Votre demande pour la chambre " + numChambre + " est terminée. Nous restons à votre entière disposition.";
            saveNotification(demande.getClient(), demande, titre, msg);

            WebSocketNotificationEvent clientEvent = buildEvent(null, "TACHE_TERMINEE", titre, msg, demande);
            webSocketHandler.sendToUser(demande.getClient().getIdUtilisateur(), clientEvent);
        }

        // Notifier Staff et Réception
        String titreStaff = "Tâche terminée ✓";
        String msgStaff = demande.getReferenceDemande() + " résolue en " + tempsReel + " min (Ch. " + numChambre + ").";
        WebSocketNotificationEvent staffEvent = buildEvent(null, "TACHE_TERMINEE_STAFF", titreStaff, msgStaff, demande);

        if (demande.getService() != null && demande.getService().getDepartement() != null) {
            webSocketHandler.sendToDepartment(demande.getService().getDepartement().getIdDepartement(), staffEvent);
        }
        webSocketHandler.sendToReceptionAndAdmin(staffEvent);
    }

    // =========================================================================
    // 5. ÉVÉNEMENT : DEMANDE REJETÉE
    // =========================================================================
    @Transactional
    public void notifyTaskRejected(Demande demande, String motif) {
        String serviceNom = demande.getService() != null ? demande.getService().getNom() : "Service";
        String motifStr = motif != null && !motif.isBlank() ? motif : "Indisponibilité exceptionnelle du service";

        if (demande.getClient() != null) {
            String titre = "Demande non réalisable : " + serviceNom;
            String msg = "Votre demande " + demande.getReferenceDemande() + " n'a pas pu être honorée. Motif : " + motifStr;
            saveNotification(demande.getClient(), demande, titre, msg);

            WebSocketNotificationEvent event = buildEvent(null, "TACHE_REJETEE", titre, msg, demande);
            webSocketHandler.sendToUser(demande.getClient().getIdUtilisateur(), event);
        }
    }

    // =========================================================================
    // 6. ÉVÉNEMENT : DEMANDE ANNULÉE PAR LE CLIENT
    // =========================================================================
    @Transactional
    public void notifyTaskCancelled(Demande demande, String motif) {
        String numChambre = getChambreNum(demande);
        String serviceNom = demande.getService() != null ? demande.getService().getNom() : "Service";
        String motifStr = motif != null && !motif.isBlank() ? motif : "Annulée par le client";

        String titre = "Demande annulée par le client";
        String msg = "Chambre " + numChambre + " · " + serviceNom + " [" + demande.getReferenceDemande() + "]. Motif : " + motifStr;

        WebSocketNotificationEvent event = buildEvent(null, "TACHE_ANNULEE", titre, msg, demande);

        if (demande.getService() != null && demande.getService().getDepartement() != null) {
            webSocketHandler.sendToDepartment(demande.getService().getDepartement().getIdDepartement(), event);
        }
        webSocketHandler.sendToReceptionAndAdmin(event);
    }
}

