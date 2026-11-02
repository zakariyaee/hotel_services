package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.service.ServiceRequest;
import com.hotelservicesbackend.dto.service.ServiceResponse;
import com.hotelservicesbackend.entity.Departement;
import com.hotelservicesbackend.entity.Service;
import com.hotelservicesbackend.repository.DepartementRepository;
import com.hotelservicesbackend.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceHotelService {

    private final ServiceRepository repository;
    private final DepartementRepository departementRepository;

    public ServiceHotelService(ServiceRepository repository, DepartementRepository departementRepository) {
        this.repository = repository;
        this.departementRepository = departementRepository;
    }

    public List<ServiceResponse> findAllResponses() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ServiceResponse> findAvailableResponses() {
        return repository.findByDisponibiliteTrue().stream().map(this::toResponse).toList();
    }

    public List<ServiceResponse> findByDepartementResponses(Long departementId) {
        return repository.findByDepartement_IdDepartement(departementId).stream().map(this::toResponse).toList();
    }

    public ServiceResponse findByIdResponse(Long id) {
        return toResponse(findById(id));
    }

    public Service findById(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Service avec ID " + id + " introuvable."));
    }

    public ServiceResponse createFromDto(ServiceRequest dto) {
        Departement departement = departementRepository.findById(dto.idDepartement())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Département introuvable."));

        Service service = new Service();
        service.setNom(dto.nom());
        service.setDescription(dto.description());
        service.setCategorie(dto.categorie() != null ? dto.categorie() : "Général");
        service.setDureeEstimee(dto.dureeEstimee() > 0 ? dto.dureeEstimee() : 30);
        service.setPrixIndicatif(dto.prixIndicatif());
        service.setDisponibilite(dto.disponibilite() != null ? dto.disponibilite() : true);
        service.setNecessiteSupervision(dto.necessiteSupervision() != null ? dto.necessiteSupervision() : false);
        service.setImageUrl(dto.imageUrl());
        service.setDepartement(departement);

        return toResponse(repository.save(service));
    }

    public ServiceResponse updateFromDto(Long id, ServiceRequest dto) {
        Service service = findById(id);

        Departement departement = departementRepository.findById(dto.idDepartement())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Département introuvable."));

        service.setNom(dto.nom());
        service.setDescription(dto.description());
        service.setCategorie(dto.categorie() != null ? dto.categorie() : service.getCategorie());
        service.setDureeEstimee(dto.dureeEstimee());
        service.setPrixIndicatif(dto.prixIndicatif());
        service.setDisponibilite(dto.disponibilite() != null ? dto.disponibilite() : service.isDisponibilite());
        service.setNecessiteSupervision(dto.necessiteSupervision() != null ? dto.necessiteSupervision() : service.isNecessiteSupervision());
        service.setImageUrl(dto.imageUrl());
        service.setDepartement(departement);

        return toResponse(repository.save(service));
    }

    public ServiceResponse toggleDisponibilite(Long id) {
        Service service = findById(id);
        service.setDisponibilite(!service.isDisponibilite());
        return toResponse(repository.save(service));
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    public ServiceResponse toResponse(Service s) {
        return new ServiceResponse(
                s.getIdService(),
                s.getNom(),
                s.getDescription(),
                s.getCategorie() != null ? s.getCategorie() : "Services Généraux",
                s.getDureeEstimee(),
                s.getPrixIndicatif(),
                s.isDisponibilite(),
                s.isNecessiteSupervision(),
                s.getImageUrl(),
                s.getDepartement() != null ? s.getDepartement().getIdDepartement() : null,
                s.getDepartement() != null ? s.getDepartement().getNom() : "Non rattaché"
        );
    }
}
