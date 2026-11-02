package com.hotelservicesbackend.dto.admin;

import com.hotelservicesbackend.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminUtilisateurRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        String motDePasse,

        String tel,

        @NotNull(message = "Le rôle est obligatoire")
        Role role,

        Long idDepartement,

        String poste
) {}

