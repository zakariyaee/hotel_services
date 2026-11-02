package com.hotelservicesbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idService;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String categorie;

    private int dureeEstimee;

    private Double prixIndicatif;

    private boolean disponibilite = true;

    /**
     * Règle métier :
     * true  = Nécessite obligatoirement une affectation manuelle par le superviseur (Personnel)
     * false = Tâche directe que tout employé du département peut s'auto-attribuer directement
     */
    @Column(nullable = false)
    private boolean necessiteSupervision = false;

    @Column(length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDepartement")
    private Departement departement;

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Demande> demandes = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CaracteristiquesServices> caracteristiquesServices = new ArrayList<>();
}
