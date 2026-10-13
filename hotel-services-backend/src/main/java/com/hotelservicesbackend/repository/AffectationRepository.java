package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    List<Affectation> findByDemande_IdDemande(Long idDemande);
    List<Affectation> findByAgent_IdUtilisateur(Long idAgent);
}
