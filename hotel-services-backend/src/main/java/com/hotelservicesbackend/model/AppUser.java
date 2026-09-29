package com.hotelservicesbackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(length = 30)
    private String role;
}
