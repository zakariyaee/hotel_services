package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
}
