package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.model.Chambre;
import com.hotelservicesbackend.repository.ChambreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chambres")
@CrossOrigin(origins = "http://localhost:4200")
public class ChambreController {

    private final ChambreRepository chambreRepository;

    public ChambreController(ChambreRepository chambreRepository) {
        this.chambreRepository = chambreRepository;
    }

    @GetMapping
    public List<Chambre> getAllChambres() {
        return chambreRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chambre> getChambreById(@PathVariable Long id) {
        return chambreRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Chambre> createChambre(@RequestBody Chambre chambre) {
        Chambre saved = chambreRepository.save(chambre);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
