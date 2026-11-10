package com.hotelservicesbackend.dto.admin;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateReservationRequest(
        Long idClient,

        String clientNom,
        String clientPrenom,
        String clientEmail,

        String clientTel,

        @NotNull(message = "La chambre est obligatoire")
        Long idChambre,

        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,

        @NotNull(message = "La date de fin est obligatoire")
        LocalDate dateFin,

        String typeReservation
) {}

