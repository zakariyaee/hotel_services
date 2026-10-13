package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.auth.CreateUtilisateurRequest;
import com.hotelservicesbackend.entity.Utilisateur;
import com.hotelservicesbackend.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "http://localhost:4200")
public class UtilisateurController {
    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Utilisateur create(@RequestBody CreateUtilisateurRequest request) {
        return service.create(request);
    }
}
