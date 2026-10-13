package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.model.Reservation;
import com.hotelservicesbackend.model.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClient_IdUtilisateur(Long idClient);
    List<Reservation> findByStatut(StatutReservation statut);
}
