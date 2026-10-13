package com.hotelservicesbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "caracteristiques_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaracteristiquesServices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCaracteristiqueService;

    @Column(length = 255)
    private String valeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idService", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCaracteristique", nullable = false)
    private Caracteristiques caracteristique;
}
