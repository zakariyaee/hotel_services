package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.admin.AdminUtilisateurRequest;
import com.hotelservicesbackend.dto.admin.AdminUtilisateurResponse;
import com.hotelservicesbackend.service.AdminUtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs")
public class AdminUtilisateurController {

    private final AdminUtilisateurService service;

    public AdminUtilisateurController(AdminUtilisateurService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdminUtilisateurResponse> getAll() {
        return service.getAllUtilisateurs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUtilisateurResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUtilisateurById(id));
    }

    @PostMapping
    public ResponseEntity<AdminUtilisateurResponse> create(@Valid @RequestBody AdminUtilisateurRequest request) {
        AdminUtilisateurResponse response = service.createUtilisateur(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUtilisateurResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminUtilisateurRequest request
    ) {
        AdminUtilisateurResponse response = service.updateUtilisateur(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteUtilisateur(id);
    }

    @PostMapping("/{idUtilisateur}/assign-departement/{idDepartement}")
    public ResponseEntity<AdminUtilisateurResponse> assignDepartement(
            @PathVariable Long idUtilisateur,
            @PathVariable Long idDepartement,
            @RequestParam(required = false) String poste
    ) {
        AdminUtilisateurResponse response = service.assignDepartement(idUtilisateur, idDepartement, poste);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{idUtilisateur}/unassign-departement")
    public ResponseEntity<AdminUtilisateurResponse> unassignDepartement(@PathVariable Long idUtilisateur) {
        AdminUtilisateurResponse response = service.unassignDepartement(idUtilisateur);
        return ResponseEntity.ok(response);
    }
}

