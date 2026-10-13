package com.hotelservicesbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "personnel")
@PrimaryKeyJoinColumn(name = "idUtilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Personnel extends Employe {

    private boolean access;

    @Column(length = 100)
    private String niveauAcces;

    private boolean actif;
}
