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
@Table(name = "departement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDepartement;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String horairesOuverture;

    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Service> services = new ArrayList<>();

    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Employe> employes = new ArrayList<>();
}
