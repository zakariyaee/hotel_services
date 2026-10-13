package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.auth.LoginRequest;
import com.hotelservicesbackend.dto.auth.LoginResponse;
import com.hotelservicesbackend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {
    private final UtilisateurRepository userRepository;
    public AuthService(UtilisateurRepository userRepository) { this.userRepository = userRepository; }

    public Optional<LoginResponse> login(LoginRequest request) {
        return userRepository.findByEmailAndMotDePasse(request.email(), request.motDePasse())
                .map(user -> new LoginResponse(user.getEmail(), user.getRole().name()));
    }
}
