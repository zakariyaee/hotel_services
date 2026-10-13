package com.hotelservicesbackend.dto.auth;

import com.hotelservicesbackend.entity.enums.Role;

public record CreateUtilisateurRequest(
        String nom,
        String prenom,
        String email,
        String motDePasse,
        String tel,
        Role role
) {}
