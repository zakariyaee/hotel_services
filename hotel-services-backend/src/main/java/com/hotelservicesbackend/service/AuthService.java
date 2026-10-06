package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.auth.LoginRequest;
import com.hotelservicesbackend.dto.auth.LoginResponse;
import com.hotelservicesbackend.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    public AuthService(AppUserRepository userRepository) { this.userRepository = userRepository; }

    public Optional<LoginResponse> login(LoginRequest request) {
        return userRepository.findByUsernameAndPassword(request.username(), request.password())
                .map(user -> new LoginResponse(user.getUsername(), normalizeRole(user.getRole())));
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return "CLIENT";
        return role.equalsIgnoreCase("GUEST") ? "CLIENT" : role.toUpperCase();
    }
}
