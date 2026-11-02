package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.admin.AdminUtilisateurRequest;
import com.hotelservicesbackend.dto.admin.AdminUtilisateurResponse;
import com.hotelservicesbackend.entity.*;
import com.hotelservicesbackend.entity.enums.Role;
import com.hotelservicesbackend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminUtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final PersonnelRepository personnelRepository;
    private final ReceptionnisteRepository receptionnisteRepository;
    private final ClientRepository clientRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUtilisateurService(
            UtilisateurRepository utilisateurRepository,
            EmployeRepository employeRepository,
            PersonnelRepository personnelRepository,
            ReceptionnisteRepository receptionnisteRepository,
            ClientRepository clientRepository,
            DepartementRepository departementRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
        this.personnelRepository = personnelRepository;
        this.receptionnisteRepository = receptionnisteRepository;
        this.clientRepository = clientRepository;
        this.departementRepository = departementRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUtilisateurResponse> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminUtilisateurResponse getUtilisateurById(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable: " + id));
        return mapToResponse(u);
    }

    public AdminUtilisateurResponse createUtilisateur(AdminUtilisateurRequest request) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé: " + request.email());
        }

        String rawPassword = (request.motDePasse() != null && !request.motDePasse().isBlank())
                ? request.motDePasse()
                : "staff123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Role role = request.role() != null ? request.role() : Role.EMPLOYE;

        Departement dept = null;
        if (request.idDepartement() != null) {
            dept = departementRepository.findById(request.idDepartement()).orElse(null);
        }

        Utilisateur savedUser;

        switch (role) {
            case PERSONNEL -> {
                Personnel personnel = new Personnel();
                fillBaseUser(personnel, request, encodedPassword, role);
                personnel.setPoste(request.poste() != null && !request.poste().isBlank() ? request.poste() : "Superviseur");
                personnel.setDepartement(dept);
                personnel.setActif(true);
                personnel.setAccess(true);
                personnel.setNiveauAcces("SUPERVISEUR_DEPARTEMENT");
                savedUser = personnelRepository.save(personnel);
            }
            case EMPLOYE, STAFF -> {
                Employe employe = new Employe();
                fillBaseUser(employe, request, encodedPassword, role);
                employe.setPoste(request.poste() != null && !request.poste().isBlank() ? request.poste() : "Agent");
                employe.setDepartement(dept);
                savedUser = employeRepository.save(employe);
            }
            case RECEPTIONNISTE -> {
                Receptionniste recp = new Receptionniste();
                fillBaseUser(recp, request, encodedPassword, role);
                recp.setPoste(request.poste() != null && !request.poste().isBlank() ? request.poste() : "Réceptionniste");
                recp.setPosteReception(request.poste() != null && !request.poste().isBlank() ? request.poste() : "Hall Principal");
                recp.setExtensionTelephonique("100" + (int)(Math.random() * 90 + 10));
                recp.setDepartement(dept);
                savedUser = receptionnisteRepository.save(recp);
            }
            case CLIENT -> {
                Client client = new Client();
                fillBaseUser(client, request, encodedPassword, role);
                client.setLanguePreferee("FR");
                client.setReservationStatut(true);
                client.setPreferenceNotification(true);
                savedUser = clientRepository.save(client);
            }
            default -> { // ADMIN
                Utilisateur admin = new Utilisateur();
                fillBaseUser(admin, request, encodedPassword, role);
                savedUser = utilisateurRepository.save(admin);
            }
        }

        return mapToResponse(savedUser);
    }

    public AdminUtilisateurResponse updateUtilisateur(Long id, AdminUtilisateurRequest request) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable: " + id));

        // Vérifier l'unicité de l'email si modifié
        if (!u.getEmail().equalsIgnoreCase(request.email()) && utilisateurRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà pris: " + request.email());
        }

        u.setNom(request.nom());
        u.setPrenom(request.prenom());
        u.setEmail(request.email());
        u.setTel(request.tel());

        if (request.motDePasse() != null && !request.motDePasse().isBlank()) {
            u.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        }

        if (request.role() != null) {
            u.setRole(request.role());
        }

        // Gestion de l'affectation au département et du poste pour les employés/personnel
        Optional<Employe> empOpt = employeRepository.findById(id);
        if (empOpt.isPresent()) {
            Employe emp = empOpt.get();
            if (request.poste() != null && !request.poste().isBlank()) {
                emp.setPoste(request.poste());
            }
            if (request.idDepartement() != null) {
                Departement dept = departementRepository.findById(request.idDepartement()).orElse(null);
                emp.setDepartement(dept);
            } else {
                emp.setDepartement(null);
            }
            employeRepository.save(emp);
        }

        Utilisateur saved = utilisateurRepository.save(u);
        return mapToResponse(saved);
    }

    public void deleteUtilisateur(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable: " + id));
        utilisateurRepository.delete(u);
    }

    public AdminUtilisateurResponse assignDepartement(Long idUtilisateur, Long idDepartement, String poste) {
        Utilisateur u = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable: " + idUtilisateur));

        // Vérification des rôles : seuls les membres du personnel / employés / réceptionnistes peuvent être affectés
        if (u.getRole() == Role.CLIENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'affecter un compte client à un département opérationnel.");
        }

        Departement dept = departementRepository.findById(idDepartement)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Département introuvable: " + idDepartement));

        Optional<Employe> empOpt = employeRepository.findById(idUtilisateur);
        Employe emp;
        if (empOpt.isPresent()) {
            emp = empOpt.get();
        } else {
            emp = new Employe();
            emp.setIdUtilisateur(u.getIdUtilisateur());
            emp.setNom(u.getNom());
            emp.setPrenom(u.getPrenom());
            emp.setEmail(u.getEmail());
            emp.setMotDePasse(u.getMotDePasse());
            emp.setTel(u.getTel());
            emp.setRole(u.getRole());
        }

        emp.setDepartement(dept);
        if (poste != null && !poste.isBlank()) {
            emp.setPoste(poste);
        } else if (emp.getPoste() == null || emp.getPoste().isBlank()) {
            emp.setPoste(u.getRole() == Role.PERSONNEL ? "Superviseur" : "Agent");
        }
        employeRepository.save(emp);

        return mapToResponse(emp);
    }

    public AdminUtilisateurResponse unassignDepartement(Long idUtilisateur) {
        Employe emp = employeRepository.findById(idUtilisateur)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé introuvable: " + idUtilisateur));
        emp.setDepartement(null);
        employeRepository.save(emp);
        return mapToResponse(emp);
    }

    private void fillBaseUser(Utilisateur u, AdminUtilisateurRequest req, String password, Role role) {
        u.setNom(req.nom());
        u.setPrenom(req.prenom());
        u.setEmail(req.email());
        u.setMotDePasse(password);
        u.setTel(req.tel());
        u.setRole(role);
    }

    private AdminUtilisateurResponse mapToResponse(Utilisateur u) {
        Long deptId = null;
        String deptNom = null;
        String poste = null;

        // Si c'est un employé (ou personnel ou réceptionniste)
        Optional<Employe> empOpt = employeRepository.findById(u.getIdUtilisateur());
        if (empOpt.isPresent()) {
            Employe emp = empOpt.get();
            poste = emp.getPoste();
            if (emp.getDepartement() != null) {
                deptId = emp.getDepartement().getIdDepartement();
                deptNom = emp.getDepartement().getNom();
            }
        }

        return new AdminUtilisateurResponse(
                u.getIdUtilisateur(),
                u.getNom(),
                u.getPrenom(),
                u.getEmail(),
                u.getTel(),
                u.getRole(),
                deptId,
                deptNom,
                poste
        );
    }
}
