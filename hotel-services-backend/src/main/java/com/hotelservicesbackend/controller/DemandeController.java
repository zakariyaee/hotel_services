package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.DemandeRequest;
import com.hotelservicesbackend.entity.Client;
import com.hotelservicesbackend.entity.Demande;
import com.hotelservicesbackend.entity.Service;
import com.hotelservicesbackend.entity.enums.PrioriteDemande;
import com.hotelservicesbackend.entity.enums.StatutDemande;
import com.hotelservicesbackend.repository.ClientRepository;
import com.hotelservicesbackend.repository.DemandeRepository;
import com.hotelservicesbackend.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/demandes")
@CrossOrigin(origins = "http://localhost:4200")
public class DemandeController {

    private final DemandeRepository demandeRepository;
    private final ServiceRepository serviceRepository;
    private final ClientRepository clientRepository;

    public DemandeController(DemandeRepository demandeRepository,
                             ServiceRepository serviceRepository,
                             ClientRepository clientRepository) {
        this.demandeRepository = demandeRepository;
        this.serviceRepository = serviceRepository;
        this.clientRepository = clientRepository;
    }

    @GetMapping
    public List<Demande> getAllDemandes() {
        return demandeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Demande> getDemandeById(@PathVariable Long id) {
        return demandeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createDemande(@RequestBody DemandeRequest request) {
        if (request.getServiceId() == null) {
            return ResponseEntity.badRequest().body("L'identifiant du service est obligatoire.");
        }

        Optional<Service> serviceOpt = serviceRepository.findById(request.getServiceId());
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Service introuvable avec l'ID: " + request.getServiceId());
        }

        // Récupérer le client spécifié ou le premier client disponible
        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId()).orElse(null);
        }
        if (client == null) {
            List<Client> clients = clientRepository.findAll();
            if (!clients.isEmpty()) {
                client = clients.get(0);
            }
        }

        if (client == null) {
            return ResponseEntity.badRequest().body("Aucun client disponible pour associer la demande.");
        }

        Service service = serviceOpt.get();
        Demande demande = new Demande();
        demande.setService(service);
        demande.setClient(client);
        demande.setDateCreation(LocalDateTime.now());
        demande.setStatut(StatutDemande.EN_ATTENTE);
        demande.setPriorite(request.getPriorite() != null ? request.getPriorite() : PrioriteDemande.NORMALE);
        demande.setTempsTraitementEstime(request.getTempsTraitementEstime() != null ?
                request.getTempsTraitementEstime() : service.getDureeEstimee());

        String ref = request.getReferenceDemande();
        if (ref == null || ref.isBlank()) {
            ref = "DEM-" + System.currentTimeMillis();
        }
        demande.setReferenceDemande(ref);

        Demande saved = demandeRepository.save(demande);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
