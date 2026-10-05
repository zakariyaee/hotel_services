package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.entity.AppUser;
import com.hotelservicesbackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        AppUser user = authService.login(request.username(), request.password());
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Nom d'utilisateur ou mot de passe incorrect"));
        return ResponseEntity.ok(Map.of("username", user.getUsername(), "role", user.getRole()));
    }
    public record LoginRequest(String username, String password) {}
}
