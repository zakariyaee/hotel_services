package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.entity.ClientAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientAccessTokenRepository extends JpaRepository<ClientAccessToken, Long> {
    Optional<ClientAccessToken> findByTokenHash(String tokenHash);
}
