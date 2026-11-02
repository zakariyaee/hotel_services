package com.hotelservicesbackend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GuestAccessRequest(
        @NotBlank(message = "Le code d'accès de réservation est obligatoire")
        String accessCode
) {}
