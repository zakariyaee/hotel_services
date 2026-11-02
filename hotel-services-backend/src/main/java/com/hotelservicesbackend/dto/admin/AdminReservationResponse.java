package com.hotelservicesbackend.dto.admin;

import java.time.LocalDate;

public record AdminReservationResponse(
        Long idReservation,
        LocalDate dateDebut,
        LocalDate dateFin,
        String statut,
        String typeReservation,
        Long idClient,
        String clientNom,
        String clientPrenom,
        String clientEmail,
        String clientTel,
        Long idChambre,
        String numChambre,
        String typeChambre,
        String accessCode,
        String qrCodeBase64,
        String loginUrl
) {}
