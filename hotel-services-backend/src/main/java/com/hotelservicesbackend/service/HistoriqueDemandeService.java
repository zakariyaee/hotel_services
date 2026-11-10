package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.HistoriqueDemandeResponse;
import com.hotelservicesbackend.entity.Demande;
import com.hotelservicesbackend.entity.HistoriqueDemande;
import com.hotelservicesbackend.entity.enums.StatutDemande;
import com.hotelservicesbackend.repository.HistoriqueDemandeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoriqueDemandeService {

    private final HistoriqueDemandeRepository repository;

    public HistoriqueDemandeService(HistoriqueDemandeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(Demande demande, StatutDemande ancienStatut, StatutDemande nouveauStatut,
                       String auteur, String commentaire) {
        if (nouveauStatut == null || nouveauStatut == ancienStatut) return;
        HistoriqueDemande event = new HistoriqueDemande();
        event.setDemande(demande);
        event.setAncienStatut(ancienStatut);
        event.setNouveauStatut(nouveauStatut);
        event.setAuteur(auteur);
        event.setCommentaire(commentaire);
        repository.save(event);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueDemandeResponse> findByDemande(Long idDemande) {
        return repository.findByDemande_IdDemandeOrderByDateChangementAsc(idDemande).stream()
                .map(event -> new HistoriqueDemandeResponse(
                        event.getIdHistorique(), event.getAncienStatut(), event.getNouveauStatut(),
                        event.getDateChangement(), event.getAuteur(), event.getCommentaire()))
                .toList();
    }
}
