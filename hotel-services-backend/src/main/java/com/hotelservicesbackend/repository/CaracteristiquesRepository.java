package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.entity.Caracteristiques;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaracteristiquesRepository extends JpaRepository<Caracteristiques, Long> {
}
