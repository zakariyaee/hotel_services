package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.entity.Departement;
import com.hotelservicesbackend.repository.DepartementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departements")
@CrossOrigin(origins = "http://localhost:4200")
public class DepartementController {

    private final DepartementRepository departementRepository;

    public DepartementController(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    @GetMapping
    public List<Departement> getAllDepartements() {
        return departementRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departement> getDepartementById(@PathVariable Long id) {
        return departementRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Departement> createDepartement(@RequestBody Departement departement) {
        Departement saved = departementRepository.save(departement);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
