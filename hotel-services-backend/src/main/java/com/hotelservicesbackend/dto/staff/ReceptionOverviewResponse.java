package com.hotelservicesbackend.dto.staff;

public record ReceptionOverviewResponse(
        long totalDemandes,
        long demandesEnAttente,
        long demandesEnCours,
        long demandesTerminees,
        long alertesRetard,
        long totalChambresOccupees
) {}

