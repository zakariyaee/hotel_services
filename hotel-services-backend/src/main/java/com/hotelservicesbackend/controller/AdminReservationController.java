package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.admin.AdminReservationResponse;
import com.hotelservicesbackend.dto.admin.CreateReservationRequest;
import com.hotelservicesbackend.service.AdminReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    public AdminReservationController(AdminReservationService adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    @GetMapping
    public List<AdminReservationResponse> getAll() {
        return adminReservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminReservationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(adminReservationService.getReservationById(id));
    }

    @PostMapping
    public ResponseEntity<AdminReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        AdminReservationResponse response = adminReservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

