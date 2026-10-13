package com.hotelservicesbackend.entity;

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
@Table(name = "client")
@PrimaryKeyJoinColumn(name = "idUtilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client extends Utilisateur {

    private boolean reservationStatut;

    @Column(length = 10)
    private String languePreferee;

    private boolean preferenceNotification;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Demande> demandes = new ArrayList<>();
}
