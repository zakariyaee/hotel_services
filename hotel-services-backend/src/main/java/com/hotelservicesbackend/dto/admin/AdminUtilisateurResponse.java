package com.hotelservicesbackend.dto.admin;

import com.hotelservicesbackend.entity.enums.Role;

public record AdminUtilisateurResponse(
        Long idUtilisateur,
        String nom,
        String prenom,
        String email,
        String tel,
        Role role,
        Long idDepartement,
        String departementNom,
        String poste
) {}

