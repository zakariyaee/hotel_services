package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.entity.Departement;
import com.hotelservicesbackend.service.DepartementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departements")
@CrossOrigin(origins = "http://localhost:4200")
public class DepartementController {

    private final DepartementService service;

    public DepartementController(DepartementService service) {
        this.service = service;
    }

    @GetMapping
    public List<Departement> getAllDepartements() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departement> getDepartementById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Departement> createDepartement(@RequestBody Departement departement) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(departement));
    }

    @PutMapping("/{id}")
    public Departement update(@PathVariable Long id, @RequestBody Departement departement) {
        return service.update(id, departement);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
