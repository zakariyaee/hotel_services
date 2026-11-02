package com.hotelservicesbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    public void sendReservationWelcomeEmail(
            String recipientEmail,
            String clientName,
            String numChambre,
            String typeChambre,
            LocalDate dateDebut,
            LocalDate dateFin,
            String accessCode,
            String loginUrl,
            String qrCodeBase64
    ) {
        log.info("================================================================================");
        log.info("📧 ENVOI D'EMAIL DE CONFIRMATION DE SÉJOUR");
        log.info("Destinataire : {}", recipientEmail);
        log.info("Client : {}", clientName);
        log.info("Chambre : {} ({})", numChambre, typeChambre);
        log.info("Période : Du {} au {}", dateDebut, dateFin);
        log.info("--------------------------------------------------------------------------------");
        log.info("🔑 IDENTIFIANTS DE CONNEXION DIGITAUX :");
        log.info("   • Identifiant (Email) : {}", recipientEmail);
        log.info("   • Mot de passe (Code séjour) : {}", accessCode);
        log.info("   • Lien d'auto-connexion directe : {}", loginUrl);
        log.info("   • Pass QR Code : [Généré avec succès]");
        log.info("================================================================================");
    }
}
