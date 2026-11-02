package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.service.ServiceRequest;
import com.hotelservicesbackend.dto.service.ServiceResponse;
import com.hotelservicesbackend.service.ServiceHotelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceHotelService service;

    public ServiceController(ServiceHotelService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceResponse> getAllServices() {
        return service.findAllResponses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findByIdResponse(id));
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.createFromDto(request));
    }

    @GetMapping("/disponibles")
    public List<ServiceResponse> getAvailableServices() {
        return service.findAvailableResponses();
    }

    @GetMapping("/departement/{id}")
    public List<ServiceResponse> getByDepartement(@PathVariable Long id) {
        return service.findByDepartementResponses(id);
    }

    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        return service.updateFromDto(id, request);
    }

    @PatchMapping("/{id}/toggle")
    public ServiceResponse toggleDisponibilite(@PathVariable Long id) {
        return service.toggleDisponibilite(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
