package com.hotelservicesbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hotelservicesbackend.model.enums.PrioriteDemande;
import com.hotelservicesbackend.model.enums.StatutDemande;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDemande;

    @Column(nullable = false, unique = true, length = 100)
    private String referenceDemande;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    private LocalDateTime datePriseEnCharge;

    private LocalDateTime dateDebutTraitement;

    private LocalDateTime dateResolution;

    private LocalDateTime dateCloture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrioriteDemande priorite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutDemande statut;

    @Column(columnDefinition = "TEXT")
    private String motifRejet;

    @Column(columnDefinition = "TEXT")
    private String motifAnnulation;

    private int tempsTraitementEstime;

    private Integer tempsTraitementReel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idService", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReceptionniste")
    private Receptionniste receptionniste;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Affectation> affectations = new ArrayList<>();

    @OneToMany(mappedBy = "demande", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Notification> notifications = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutDemande.EN_ATTENTE;
        }
        if (priorite == null) {
            priorite = PrioriteDemande.NORMALE;
        }
        if (referenceDemande == null || referenceDemande.isBlank()) {
            referenceDemande = "DEM-" + System.currentTimeMillis();
        }
    }
}
