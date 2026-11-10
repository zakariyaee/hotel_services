package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.DemandeRequest;
import com.hotelservicesbackend.dto.DemandeUpdateRequest;
import com.hotelservicesbackend.dto.HistoriqueDemandeResponse;
import com.hotelservicesbackend.entity.Demande;
import com.hotelservicesbackend.service.DemandeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {
    private final DemandeService service;
    private final com.hotelservicesbackend.service.HistoriqueDemandeService historiqueService;

    public DemandeController(DemandeService service, com.hotelservicesbackend.service.HistoriqueDemandeService historiqueService) {
        this.service = service;
        this.historiqueService = historiqueService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PERSONNEL','STAFF','EMPLOYE','RECEPTIONNISTE')")
    public List<Demande> getAll() { return service.findAll(); }

    @GetMapping("/client/{clientId}")
    public List<Demande> getByClient(@PathVariable Long clientId, Authentication authentication) {
        return service.findByClient(clientId, authentication.getName());
    }

    @GetMapping("/{id}")
    public Demande getById(@PathVariable Long id, Authentication authentication) {
        return service.findByIdForRequester(id, authentication.getName());
    }

    @GetMapping("/{id}/historique")
    public List<HistoriqueDemandeResponse> getHistorique(@PathVariable Long id, Authentication authentication) {
        service.findByIdForRequester(id, authentication.getName());
        return historiqueService.findByDemande(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Demande create(@RequestBody DemandeRequest request, Authentication authentication) {
        return service.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public Demande update(@PathVariable Long id, @RequestBody DemandeUpdateRequest request, Authentication authentication) {
        return service.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { service.delete(id); }
}

