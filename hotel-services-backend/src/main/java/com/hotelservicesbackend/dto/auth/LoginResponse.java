package com.hotelservicesbackend.dto.auth;

public record LoginResponse(
        Long idUtilisateur,
        String email,
        String role,
        String token,
        Long idDepartement,
        String departementNom
) {}
