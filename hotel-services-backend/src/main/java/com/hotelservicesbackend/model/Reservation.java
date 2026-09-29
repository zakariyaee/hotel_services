package com.hotelservicesbackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "reservation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guest_name", length = 100)
    private String guestName;

    @Column(name = "room_number", length = 10)
    private String roomNumber;

    @Column(name = "check_in")
    private LocalDate checkIn;

    @Column(name = "check_out")
    private LocalDate checkOut;

    @Column(name = "access_code", length = 20, unique = true)
    private String accessCode;

    @Column(length = 20)
    private String status;
}
