package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByDisponibiliteTrue();
    List<Service> findByDepartement_IdDepartement(Long idDepartement);
}
