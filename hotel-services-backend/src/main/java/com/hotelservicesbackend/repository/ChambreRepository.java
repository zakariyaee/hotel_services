package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.model.Chambre;
import com.hotelservicesbackend.model.enums.StatutChambre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Long> {
    Optional<Chambre> findByNumChambre(String numChambre);
    List<Chambre> findByStatut(StatutChambre statut);
}
