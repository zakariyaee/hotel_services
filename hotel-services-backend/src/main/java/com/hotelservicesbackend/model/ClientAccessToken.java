package com.hotelservicesbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_access_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @Column(nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateExpiration;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false)
    private int nbrUtilisations = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReservation", nullable = false)
    private Reservation reservation;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
