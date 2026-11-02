package com.hotelservicesbackend.dto.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ServiceRequest(
        @NotBlank(message = "Le nom du service est obligatoire")
        String nom,

        String description,
        String categorie,
        int dureeEstimee,
        Double prixIndicatif,
        Boolean disponibilite,
        Boolean necessiteSupervision,
        String imageUrl,

        @NotNull(message = "L'identifiant du département est obligatoire")
        Long idDepartement
) {}

