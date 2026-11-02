package com.hotelservicesbackend.dto.staff;

public record StaffMemberResponse(
        Long idUtilisateur,
        String nom,
        String prenom,
        String email,
        String tel,
        String poste,
        Long idDepartement,
        String nomDepartement,
        boolean disponible,
        long nbTachesEnCours
) {}

