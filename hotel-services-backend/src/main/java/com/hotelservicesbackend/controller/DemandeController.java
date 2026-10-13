package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.DemandeRequest;
import com.hotelservicesbackend.dto.DemandeUpdateRequest;
import com.hotelservicesbackend.entity.Demande;
import com.hotelservicesbackend.service.DemandeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
@CrossOrigin(origins = "http://localhost:4200")
public class DemandeController {
    private final DemandeService service;

    public DemandeController(DemandeService service) { this.service = service; }

    @GetMapping
    public List<Demande> getAll() { return service.findAll(); }

    @GetMapping("/client/{clientId}")
    public List<Demande> getByClient(@PathVariable Long clientId) { return service.findByClient(clientId); }

    @GetMapping("/{id}")
    public Demande getById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Demande create(@RequestBody DemandeRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    public Demande update(@PathVariable Long id, @RequestBody DemandeUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
