package com.hotelservicesbackend.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Base64;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.mail.from:Grand Hôtel <hotel.services.contact@gmail.com>}")
    private String mailFrom;

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
        log.info("📧 PRÉPARATION DE L'EMAIL DE CONFIRMATION DE SÉJOUR");
        log.info("Destinataire : {}", recipientEmail);
        log.info("Client : {}", clientName);
        log.info("Chambre : {} ({})", numChambre, typeChambre);
        log.info("Période : Du {} au {}", dateDebut, dateFin);
        log.info("Code d'accès séjour : {}", accessCode);
        log.info("Lien auto-login : {}", loginUrl);
        log.info("================================================================================");

        // Si la configuration SMTP n'est pas encore renseignée par l'utilisateur
        if (mailSender == null || mailPassword == null || mailPassword.trim().isEmpty()) {
            log.warn("⚠️ SMTP : Aucun mot de passe SMTP configuré dans application.properties.");
            log.warn("   Pour envoyer de vrais emails vers {}, configurez 'spring.mail.username' et 'spring.mail.password'.", recipientEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(recipientEmail);
            helper.setSubject("Bienvenue au Grand Hôtel — Vos accès & Pass Numérique pour la Chambre " + numChambre);

            String htmlBody = buildHtmlTemplate(clientName, numChambre, typeChambre, dateDebut, dateFin, recipientEmail, accessCode, loginUrl);
            helper.setText(htmlBody, true);

            // Attacher l'image du QR Code si disponible
            if (qrCodeBase64 != null && qrCodeBase64.contains(",")) {
                try {
                    String base64Image = qrCodeBase64.split(",")[1];
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                    helper.addAttachment("Pass-QRCode-Chambre-" + numChambre + ".png", new ByteArrayResource(imageBytes), "image/png");
                } catch (Exception ex) {
                    log.warn("Impossible d'attacher le QR code en pièce jointe : {}", ex.getMessage());
                }
            }

            mailSender.send(message);
            log.info("✅ EMAIL SMTP ENVOYÉ AVEC SUCCÈS À {}", recipientEmail);

        } catch (Exception e) {
            log.error("❌ ÉCHEC DE L'ENVOI SMTP : {}", e.getMessage());
            log.info("ℹ️ Les identifiants restent visibles dans l'interface administrateur et dans les logs.");
        }
    }

    private String buildHtmlTemplate(
            String clientName,
            String numChambre,
            String typeChambre,
            LocalDate dateDebut,
            LocalDate dateFin,
            String email,
            String accessCode,
            String loginUrl
    ) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
              <meta charset="UTF-8">
              <style>
                body { margin: 0; padding: 0; background-color: #f6f3ec; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #172a24; }
                .email-wrapper { max-width: 600px; margin: 30px auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; border: 1px solid #e5dfd5; }
                .email-header { background: linear-gradient(135deg, #172a24 0%%, #0f1c18 100%%); padding: 36px 30px; text-align: center; color: #ffffff; }
                .stars { color: #d4af37; font-size: 16px; letter-spacing: 5px; }
                .hotel-title { font-size: 22px; font-weight: 700; margin: 8px 0 4px 0; letter-spacing: 2px; }
                .hotel-subtitle { font-size: 13px; color: #a4b8af; margin: 0; }
                .email-body { padding: 32px 30px; }
                .greeting { font-size: 18px; font-weight: 700; margin-bottom: 12px; }
                .intro-text { font-size: 14px; line-height: 1.6; color: #4a5c55; }
                .stay-summary { background-color: #faf8f5; border: 1px solid #ebe5dc; border-radius: 12px; padding: 18px; margin: 24px 0; }
                .stay-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
                .credentials-card { background: #172a24; color: #ffffff; border-radius: 12px; padding: 22px; margin: 24px 0; border: 2px solid #d4af37; text-align: center; }
                .cred-title { font-size: 12px; font-weight: 700; color: #d4af37; letter-spacing: 1.5px; text-transform: uppercase; margin-bottom: 14px; }
                .cred-item { margin-bottom: 12px; font-size: 14px; }
                .cred-label { color: #9bb0a6; font-size: 12px; }
                .code-display { font-size: 22px; font-weight: 800; color: #ffe699; letter-spacing: 3px; font-family: monospace; margin-top: 4px; }
                .btn-container { text-align: center; margin: 30px 0; }
                .btn-cta { display: inline-block; background-color: #c59b27; color: #ffffff !important; text-decoration: none; padding: 14px 32px; border-radius: 999px; font-weight: 700; font-size: 15px; box-shadow: 0 4px 12px rgba(197, 155, 39, 0.3); }
                .email-footer { background-color: #f1ede5; padding: 20px; text-align: center; font-size: 12px; color: #7a8a83; border-top: 1px solid #e5dfd5; }
              </style>
            </head>
            <body>
              <div class="email-wrapper">
                <div class="email-header">
                  <div class="stars">★★★★★</div>
                  <h1 class="hotel-title">GRAND HÔTEL INTRA-SERVICES</h1>
                  <p class="hotel-subtitle">Votre clé numérique d'accès aux services hôteliers</p>
                </div>
                <div class="email-body">
                  <div class="greeting">Bonjour %s,</div>
                  <p class="intro-text">
                    Nous avons le plaisir de vous confirmer votre séjour au sein de notre établissement. Afin de rendre votre expérience unique, commandez vos services en chambre (room service, conciergerie, réveil, ménage) directement depuis votre smartphone ou tablette.
                  </p>

                  <div class="stay-summary">
                    <p style="margin:0 0 10px 0; font-weight:700; color:#172a24;">Détails de votre réservation :</p>
                    <p style="margin:4px 0; font-size:13px;">• <strong>Chambre :</strong> N° %s (%s)</p>
                    <p style="margin:4px 0; font-size:13px;">• <strong>Arrivée :</strong> %s</p>
                    <p style="margin:4px 0; font-size:13px;">• <strong>Départ :</strong> %s</p>
                  </div>

                  <div class="credentials-card">
                    <div class="cred-title">Vos Identifiants de Connexion</div>
                    <div class="cred-item">
                      <div class="cred-label">Identifiant (Email)</div>
                      <div style="font-weight:600; font-size:15px; margin-top:2px;">%s</div>
                    </div>
                    <div class="cred-item">
                      <div class="cred-label">Mot de passe temporaire (Code séjour)</div>
                      <div class="code-display">%s</div>
                    </div>
                  </div>

                  <div class="btn-container">
                    <a href="%s" class="btn-cta" target="_blank">Connexion Instantanée en 1 Clic &rarr;</a>
                  </div>

                  <p class="intro-text" style="font-size:12px; text-align:center; color:#6a7c75;">
                    Vous trouverez également en pièce jointe votre <strong>Pass QR Code</strong>. Vous pouvez le scanner à tout moment avec l'appareil photo de votre smartphone pour vous reconnecter instantanément.
                  </p>
                </div>
                <div class="email-footer">
                  Grand Hôtel Intra-Services &bull; Réception ouverte 24h/24 &bull; Assistance disponible depuis votre chambre.
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                clientName,
                numChambre,
                typeChambre != null ? typeChambre : "Standard",
                dateDebut,
                dateFin,
                email,
                accessCode,
                loginUrl
        );
    }
}
