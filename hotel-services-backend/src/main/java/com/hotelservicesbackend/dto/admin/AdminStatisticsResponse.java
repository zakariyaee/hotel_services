package com.hotelservicesbackend.dto.admin;

import java.util.Map;

public record AdminStatisticsResponse(
        long totalDemandes,
        long demandesEnAttente,
        long demandesEnCours,
        long demandesTerminees,
        double tempsMoyenTraitement,
        Map<String, Long> demandesParDepartement,
        Map<String, Long> demandesParService
) {}
