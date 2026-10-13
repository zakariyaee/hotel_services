package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    List<Employe> findByDepartementIdDepartement(Long idDepartement);
}
