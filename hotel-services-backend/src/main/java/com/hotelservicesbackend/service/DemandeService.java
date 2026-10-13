package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.DemandeRequest;
import com.hotelservicesbackend.dto.DemandeUpdateRequest;
import com.hotelservicesbackend.entity.Client;
import com.hotelservicesbackend.entity.Demande;
import com.hotelservicesbackend.entity.Service;
import com.hotelservicesbackend.entity.enums.PrioriteDemande;
import com.hotelservicesbackend.entity.enums.StatutDemande;
import com.hotelservicesbackend.repository.ClientRepository;
import com.hotelservicesbackend.repository.DemandeRepository;
import com.hotelservicesbackend.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
public class DemandeService {
    private final DemandeRepository demandeRepository;
    private final ServiceRepository serviceRepository;
    private final ClientRepository clientRepository;

    public DemandeService(DemandeRepository demandeRepository, ServiceRepository serviceRepository,
                          ClientRepository clientRepository) {
        this.demandeRepository = demandeRepository;
        this.serviceRepository = serviceRepository;
        this.clientRepository = clientRepository;
    }

    public List<Demande> findAll() { return demandeRepository.findAll(); }

    public List<Demande> findByClient(Long clientId) { return demandeRepository.findByClient_IdUtilisateur(clientId); }

    public Demande findById(Long id) {
        return demandeRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable"));
    }

    public Demande create(DemandeRequest request) {
        if (request.getServiceId() == null || request.getClientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client et service obligatoires");
        }
        Service service = serviceRepository.findById(request.getServiceId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Service introuvable"));
        Client client = clientRepository.findById(request.getClientId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));

        Demande demande = new Demande();
        demande.setService(service);
        demande.setClient(client);
        demande.setDateCreation(LocalDateTime.now());
        demande.setStatut(StatutDemande.EN_ATTENTE);
        demande.setPriorite(request.getPriorite() == null ? PrioriteDemande.NORMALE : request.getPriorite());
        demande.setTempsTraitementEstime(request.getTempsTraitementEstime() == null
                ? service.getDureeEstimee() : request.getTempsTraitementEstime());
        demande.setReferenceDemande(request.getReferenceDemande());
        return demandeRepository.save(demande);
    }

    public Demande update(Long id, DemandeUpdateRequest request) {
        Demande demande = findById(id);
        if (request.priorite() != null) demande.setPriorite(request.priorite());
        if (request.statut() != null) demande.setStatut(request.statut());
        if (request.motifRejet() != null) demande.setMotifRejet(request.motifRejet());
        if (request.motifAnnulation() != null) demande.setMotifAnnulation(request.motifAnnulation());
        if (request.tempsTraitementReel() != null) demande.setTempsTraitementReel(request.tempsTraitementReel());
        demande.setDateModification(LocalDateTime.now());
        return demandeRepository.save(demande);
    }

    public void delete(Long id) { demandeRepository.delete(findById(id)); }
}
