package com.hotelservicesbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "affectation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAffectation;

    @Column(nullable = false)
    private LocalDateTime dateAffectation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReceptionniste", nullable = true)
    private Receptionniste receptionniste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAgent", nullable = true)
    private Agent agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEmploye", nullable = true)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAssigneur", nullable = true)
    private Utilisateur assigneur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDemande", nullable = false)
    private Demande demande;

    @PrePersist
    public void prePersist() {
        if (dateAffectation == null) {
            dateAffectation = LocalDateTime.now();
        }
    }
}
