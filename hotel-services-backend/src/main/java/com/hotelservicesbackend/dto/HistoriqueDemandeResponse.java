package com.hotelservicesbackend.dto;

import com.hotelservicesbackend.entity.enums.StatutDemande;

import java.time.LocalDateTime;

public record HistoriqueDemandeResponse(
        Long idHistorique,
        StatutDemande ancienStatut,
        StatutDemande nouveauStatut,
        LocalDateTime dateChangement,
        String auteur,
        String commentaire
) {}
