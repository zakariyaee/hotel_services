package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.entity.HistoriqueDemande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueDemandeRepository extends JpaRepository<HistoriqueDemande, Long> {
    List<HistoriqueDemande> findByDemande_IdDemandeOrderByDateChangementAsc(Long idDemande);
}
