package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.entity.CaracteristiquesServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaracteristiquesServicesRepository extends JpaRepository<CaracteristiquesServices, Long> {
    List<CaracteristiquesServices> findByService_IdService(Long idService);
}
