package com.hotelservicesbackend.dto.service;

public record ServiceResponse(
        Long idService,
        String nom,
        String description,
        String categorie,
        int dureeEstimee,
        Double prixIndicatif,
        boolean disponibilite,
        boolean necessiteSupervision,
        String imageUrl,
        Long idDepartement,
        String nomDepartement
) {}

