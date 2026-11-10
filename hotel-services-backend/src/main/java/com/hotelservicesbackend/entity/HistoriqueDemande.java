package com.hotelservicesbackend.entity;

import com.hotelservicesbackend.entity.enums.StatutDemande;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_demande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistorique;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idDemande", nullable = false)
    private Demande demande;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatutDemande ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutDemande nouveauStatut;

    @Column(nullable = false)
    private LocalDateTime dateChangement;

    @Column(length = 255)
    private String auteur;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @PrePersist
    public void prePersist() {
        if (dateChangement == null) dateChangement = LocalDateTime.now();
    }
}
