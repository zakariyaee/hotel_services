package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameAndPassword(String username, String password);
}
