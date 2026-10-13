package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.model.Receptionniste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceptionnisteRepository extends JpaRepository<Receptionniste, Long> {
}
