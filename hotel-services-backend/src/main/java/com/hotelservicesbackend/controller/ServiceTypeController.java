package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.model.ServiceType;
import com.hotelservicesbackend.repository.ServiceTypeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-types")
@CrossOrigin(origins = "http://localhost:4200")
public class ServiceTypeController {

    private final ServiceTypeRepository repository;

    public ServiceTypeController(ServiceTypeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ServiceType> getAll() {
        return repository.findAll();
    }
}
