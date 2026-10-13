package com.hotelservicesbackend.service;

import com.hotelservicesbackend.entity.Departement;
import com.hotelservicesbackend.repository.DepartementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DepartementService {
    private final DepartementRepository repository;

    public DepartementService(DepartementRepository repository) { this.repository = repository; }

    public List<Departement> findAll() { return repository.findAll(); }

    public Departement findById(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Département introuvable"));
    }

    public Departement create(Departement departement) { return repository.save(departement); }

    public Departement update(Long id, Departement data) {
        Departement departement = findById(id);
        departement.setNom(data.getNom());
        departement.setDescription(data.getDescription());
        departement.setHorairesOuverture(data.getHorairesOuverture());
        return repository.save(departement);
    }

    public void delete(Long id) { repository.delete(findById(id)); }
}
