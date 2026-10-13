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
@Table(name = "caracteristiques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Caracteristiques {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCaracteristique;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String typeValeur;

    @Column(length = 255)
    private String valeurParDefaut;

    private Integer valeurMinimale;

    private Integer valeurMaximale;

    @Column(length = 50)
    private String unite;

    @OneToMany(mappedBy = "caracteristique", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CaracteristiquesServices> caracteristiquesServices = new ArrayList<>();
}
