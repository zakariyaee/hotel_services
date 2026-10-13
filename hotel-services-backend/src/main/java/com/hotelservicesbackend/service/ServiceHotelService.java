package com.hotelservicesbackend.service;

import com.hotelservicesbackend.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceHotelService {
    private final ServiceRepository repository;

    public ServiceHotelService(ServiceRepository repository) {
        this.repository = repository;
    }

    public List<com.hotelservicesbackend.entity.Service> findAll() { return repository.findAll(); }

    public List<com.hotelservicesbackend.entity.Service> findAvailable() {
        return repository.findByDisponibiliteTrue();
    }

    public com.hotelservicesbackend.entity.Service findById(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Service introuvable"));
    }

    public com.hotelservicesbackend.entity.Service create(com.hotelservicesbackend.entity.Service service) {
        return repository.save(service);
    }

    public com.hotelservicesbackend.entity.Service update(Long id, com.hotelservicesbackend.entity.Service data) {
        com.hotelservicesbackend.entity.Service service = findById(id);
        service.setNom(data.getNom());
        service.setDescription(data.getDescription());
        service.setDureeEstimee(data.getDureeEstimee());
        service.setDisponibilite(data.isDisponibilite());
        service.setImageUrl(data.getImageUrl());
        service.setDepartement(data.getDepartement());
        return repository.save(service);
    }

    public void delete(Long id) { repository.delete(findById(id)); }
}
