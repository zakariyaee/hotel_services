package com.hotelservicesbackend.service;

import com.hotelservicesbackend.entity.AppUser;
import com.hotelservicesbackend.repository.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    public AuthService(AppUserRepository userRepository) { this.userRepository = userRepository; }
    public AppUser login(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password).orElse(null);
    }
}
