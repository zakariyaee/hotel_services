package com.hotelservicesbackend.dto.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long idNotification,
    String titre,
    String message,
    LocalDateTime dateCreation,
    LocalDateTime dateLecture,
    String statutEnvoi,
    Long idDemande,
    String referenceDemande,
    String type
) {}

