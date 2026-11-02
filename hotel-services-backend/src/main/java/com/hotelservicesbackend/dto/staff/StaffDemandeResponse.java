package com.hotelservicesbackend.dto.staff;

import com.hotelservicesbackend.entity.enums.PrioriteDemande;
import com.hotelservicesbackend.entity.enums.StatutDemande;

import java.time.LocalDateTime;

public record StaffDemandeResponse(
        Long idDemande,
        String referenceDemande,
        Long idService,
        String serviceNom,
        String serviceCategorie,
        int serviceDuree,
        boolean necessiteSupervision,
        Long idDepartement,
        String departementNom,
        Long idClient,
        String clientNom,
        String clientPrenom,
        String clientEmail,
        String numChambre,
        StatutDemande statut,
        PrioriteDemande priorite,
        LocalDateTime dateCreation,
        LocalDateTime datePriseEnCharge,
        LocalDateTime dateResolution,
        int tempsTraitementEstime,
        Integer tempsTraitementReel,
        Long idEmployeAssigne,
        String nomEmployeAssigne,
        String posteEmployeAssigne,
        String commentairesClient,
        boolean enRetard,
        long retardMinutes
) {}

