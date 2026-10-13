package com.hotelservicesbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "employe")
@PrimaryKeyJoinColumn(name = "idUtilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employe extends Utilisateur {

    @Column(length = 100)
    private String poste;

    private LocalDateTime heureDebut;

    private LocalDateTime heureFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDepartement")
    private Departement departement;
}
