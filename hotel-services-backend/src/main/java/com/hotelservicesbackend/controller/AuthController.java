package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.auth.LoginRequest;
import com.hotelservicesbackend.dto.auth.LoginResponse;
import com.hotelservicesbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Identifiant ou mot de passe incorrect"));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Authentification échouée"));
        }
    }
}
