package com.hotelservicesbackend.dto.staff;

public record StaffProfileResponse(
        Long idUtilisateur,
        String nom,
        String prenom,
        String poste,
        Long idDepartement,
        String departementNom
) {}
