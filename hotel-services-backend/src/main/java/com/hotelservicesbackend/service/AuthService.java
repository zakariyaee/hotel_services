package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.auth.LoginRequest;
import com.hotelservicesbackend.dto.auth.LoginResponse;
import com.hotelservicesbackend.entity.Utilisateur;
import com.hotelservicesbackend.repository.UtilisateurRepository;
import com.hotelservicesbackend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthService(
            UtilisateurRepository utilisateurRepository,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public LoginResponse login(LoginRequest request) {
        // Authentification via Spring Security (BCrypt inclus)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse())
        );

        // Récupération de l'utilisateur depuis la base
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Chargement des UserDetails pour générer le token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        // Génération du JWT avec userId et role en claims
        String token = jwtUtil.generateToken(userDetails, utilisateur.getIdUtilisateur(), utilisateur.getRole().name());

        return new LoginResponse(
                utilisateur.getIdUtilisateur(),
                utilisateur.getEmail(),
                utilisateur.getRole().name(),
                token
        );
    }
}
