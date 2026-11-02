package com.hotelservicesbackend.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateReservationRequest(
        @NotBlank(message = "Le nom du client est obligatoire")
        String clientNom,

        @NotBlank(message = "Le prénom du client est obligatoire")
        String clientPrenom,

        @NotBlank(message = "L'email du client est obligatoire")
        @Email(message = "Format d'email invalide")
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
