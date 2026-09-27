package com.hotelservicesbackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_type")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
}
