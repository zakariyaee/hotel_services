package com.hotelservicesbackend.entity;

import com.hotelservicesbackend.entity.enums.StatutNotification;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotification;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateEnvoi;

    private LocalDateTime dateLecture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutNotification statutEnvoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUtilisateur", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDemande")
    private Demande demande;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statutEnvoi == null) {
            statutEnvoi = StatutNotification.EN_ATTENTE;
        }
    }
}
