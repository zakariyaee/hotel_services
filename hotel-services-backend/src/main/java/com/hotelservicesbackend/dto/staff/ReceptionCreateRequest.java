package com.hotelservicesbackend.dto.staff;

import com.hotelservicesbackend.entity.enums.PrioriteDemande;

public record ReceptionCreateRequest(
        Long idClient,
        Long idService,
        PrioriteDemande priorite,
        String commentairesClient
) {}

