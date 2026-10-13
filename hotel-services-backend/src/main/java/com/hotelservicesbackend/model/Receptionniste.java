package com.hotelservicesbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receptionniste")
@PrimaryKeyJoinColumn(name = "idUtilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Receptionniste extends Employe {

    @Column(length = 100)
    private String posteReception;

    @Column(length = 50)
    private String extensionTelephonique;

    @OneToMany(mappedBy = "receptionniste", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Affectation> affectations = new ArrayList<>();

    @OneToMany(mappedBy = "receptionniste", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Demande> demandes = new ArrayList<>();
}
