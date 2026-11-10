package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.admin.AdminStatisticsResponse;
import com.hotelservicesbackend.entity.Demande;
import com.hotelservicesbackend.entity.enums.StatutDemande;
import com.hotelservicesbackend.repository.DemandeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    private final DemandeRepository demandeRepository;

    public AdminStatisticsController(DemandeRepository demandeRepository) {
        this.demandeRepository = demandeRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public AdminStatisticsResponse getStatistics() {
        List<Demande> demandes = demandeRepository.findAll();
        long enAttente = demandes.stream().filter(d -> d.getStatut() == StatutDemande.EN_ATTENTE).count();
        long enCours = demandes.stream().filter(d -> d.getStatut() == StatutDemande.EN_COURS || d.getStatut() == StatutDemande.PRISE_EN_CHARGE).count();
        long terminees = demandes.stream().filter(d -> d.getStatut() == StatutDemande.TERMINEE).count();
        double moyenne = demandes.stream()
                .map(Demande::getTempsTraitementReel)
                .filter(value -> value != null && value >= 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        Map<String, Long> parDepartement = countBy(demandes, demande ->
                demande.getService() != null && demande.getService().getDepartement() != null
                        ? demande.getService().getDepartement().getNom() : "Non rattaché");
        Map<String, Long> parService = countBy(demandes, demande ->
                demande.getService() != null ? demande.getService().getNom() : "Service général");

        return new AdminStatisticsResponse(demandes.size(), enAttente, enCours, terminees, moyenne, parDepartement, parService);
    }

    private Map<String, Long> countBy(List<Demande> demandes, Function<Demande, String> keyExtractor) {
        return demandes.stream().collect(Collectors.groupingBy(
                keyExtractor,
                LinkedHashMap::new,
                Collectors.counting()
        ));
    }
}
