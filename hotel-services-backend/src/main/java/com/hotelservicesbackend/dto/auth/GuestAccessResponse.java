package com.hotelservicesbackend.dto.auth;

import java.time.LocalDate;

public record GuestAccessResponse(
        Long idUtilisateur,
        String email,
        String nom,
        String prenom,
        String role,
        String token,
        Long idReservation,
        String numChambre,
        String typeChambre,
        LocalDate dateDebut,
        LocalDate dateFin
) {}

