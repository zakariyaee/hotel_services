package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.auth.CreateUtilisateurRequest;
import com.hotelservicesbackend.entity.Client;
import com.hotelservicesbackend.entity.Employe;
import com.hotelservicesbackend.entity.Utilisateur;
import com.hotelservicesbackend.entity.enums.Role;
import com.hotelservicesbackend.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UtilisateurService {
    private final UtilisateurRepository repository;

    public UtilisateurService(UtilisateurRepository repository) {
        this.repository = repository;
    }

    public Utilisateur create(CreateUtilisateurRequest request) {
        if (request.email() == null || request.email().isBlank()
                || request.motDePasse() == null || request.motDePasse().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email et mot de passe obligatoires");
        }
        if (repository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email existe déjà");
        }

        Role role = request.role() == null ? Role.CLIENT : request.role();
        Utilisateur utilisateur = switch (role) {
            case CLIENT -> new Client();
            case STAFF -> new Employe();
            case ADMIN -> new Utilisateur();
        };

        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setEmail(request.email());
        utilisateur.setMotDePasse(request.motDePasse());
        utilisateur.setTel(request.tel());
        utilisateur.setRole(role);
        return repository.save(utilisateur);
    }
}
