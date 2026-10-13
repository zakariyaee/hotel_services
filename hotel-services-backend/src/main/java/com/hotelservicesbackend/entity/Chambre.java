package com.hotelservicesbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hotelservicesbackend.entity.enums.StatutChambre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chambre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chambre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idChambre;

    @Column(nullable = false, unique = true, length = 50)
    private String numChambre;

    @Column(length = 100)
    private String typeChambre;

    private int etage;

    private int capacite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutChambre statut;

    @OneToMany(mappedBy = "chambre", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();
}
