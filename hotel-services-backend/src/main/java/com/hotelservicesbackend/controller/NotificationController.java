package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.notification.NotificationResponse;
import com.hotelservicesbackend.entity.Notification;
import com.hotelservicesbackend.entity.Utilisateur;
import com.hotelservicesbackend.repository.NotificationRepository;
import com.hotelservicesbackend.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    public NotificationController(NotificationRepository notificationRepository, UtilisateurRepository utilisateurRepository) {
        this.notificationRepository = notificationRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    private Utilisateur getAuthUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
        return utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    @GetMapping
    public List<NotificationResponse> getMyNotifications(Authentication authentication) {
        Utilisateur user = getAuthUser(authentication);
        List<Notification> list = notificationRepository.findByUtilisateur_IdUtilisateurOrderByDateCreationDesc(user.getIdUtilisateur());
        return list.stream().map(this::toResponse).toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Authentication authentication) {
        Utilisateur user = getAuthUser(authentication);
        long count = notificationRepository.countByUtilisateur_IdUtilisateurAndDateLectureIsNull(user.getIdUtilisateur());
        return Map.of("unreadCount", count);
    }

    @PatchMapping("/{id}/read")
    @Transactional
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        Utilisateur user = getAuthUser(authentication);
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable"));

        if (!notif.getUtilisateur().getIdUtilisateur().equals(user.getIdUtilisateur())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action non autorisée sur cette notification");
        }

        notif.setDateLecture(LocalDateTime.now());
        notificationRepository.save(notif);
        return ResponseEntity.ok(Map.of("message", "Notification marquée comme lue"));
    }

    @PatchMapping("/mark-all-read")
    @Transactional
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        Utilisateur user = getAuthUser(authentication);
        List<Notification> unread = notificationRepository.findByUtilisateur_IdUtilisateurAndDateLectureIsNull(user.getIdUtilisateur());
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(n -> n.setDateLecture(now));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok(Map.of("message", "Toutes les notifications ont été marquées comme lues"));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getIdNotification(),
                n.getTitre(),
                n.getMessage(),
                n.getDateCreation(),
                n.getDateLecture(),
                n.getStatutEnvoi() != null ? n.getStatutEnvoi().name() : "ENVOYEE",
                n.getDemande() != null ? n.getDemande().getIdDemande() : null,
                n.getDemande() != null ? n.getDemande().getReferenceDemande() : null,
                "NOTIFICATION"
        );
    }
}

