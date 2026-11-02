package com.hotelservicesbackend.dto.staff;

public record RoomGuestOption(
        Long idClient,
        String clientNom,
        String clientPrenom,
        String clientEmail,
        Long idChambre,
        String numChambre,
        String typeChambre,
        Long idReservation
) {}

