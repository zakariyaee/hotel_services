package com.hotelservicesbackend.dto.notification;

import java.time.LocalDateTime;

public record WebSocketNotificationEvent(
    Long idNotification,
    String type,
    String titre,
    String message,
    Long idDemande,
    String referenceDemande,
    Long idService,
    String serviceNom,
    Long idDepartement,
    String departementNom,
    Long idClient,
    String clientNom,
    String numChambre,
    String priorite,
    String statut,
    LocalDateTime timestamp
) {}

