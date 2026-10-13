package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.model.Demande;
import com.hotelservicesbackend.model.enums.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {
    Optional<Demande> findByReferenceDemande(String referenceDemande);
    List<Demande> findByClient_IdUtilisateur(Long idClient);
    List<Demande> findByService_IdService(Long idService);
    List<Demande> findByStatut(StatutDemande statut);
}
