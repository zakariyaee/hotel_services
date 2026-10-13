package com.hotelservicesbackend.dto;

import com.hotelservicesbackend.entity.enums.PrioriteDemande;
import com.hotelservicesbackend.entity.enums.StatutDemande;

public record DemandeUpdateRequest(
        PrioriteDemande priorite,
        StatutDemande statut,
        String motifRejet,
        String motifAnnulation,
        Integer tempsTraitementReel
) {}
