package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.entity.Service;
import com.hotelservicesbackend.service.ServiceHotelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:4200")
public class ServiceController {

    private final ServiceHotelService service;

    public ServiceController(ServiceHotelService service) {
        this.service = service;
    }

    @GetMapping
    public List<Service> getAllServices() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Service> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Service> createService(@RequestBody Service service) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(service));
    }

    @GetMapping("/disponibles")
    public List<Service> getAvailableServices() {
        return service.findAvailable();
    }

    @PutMapping("/{id}")
    public Service update(@PathVariable Long id, @RequestBody Service value) {
        return service.update(id, value);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
