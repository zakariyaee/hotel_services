package com.hotelservicesbackend.dto;

import com.hotelservicesbackend.model.enums.PrioriteDemande;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeRequest {
    private Long serviceId;
    private Long clientId;
    private PrioriteDemande priorite;
    private Integer tempsTraitementEstime;
    private String referenceDemande;
}
