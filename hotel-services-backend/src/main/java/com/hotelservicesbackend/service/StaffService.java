package com.hotelservicesbackend.service;

import com.hotelservicesbackend.dto.staff.*;
import com.hotelservicesbackend.entity.*;
import com.hotelservicesbackend.entity.enums.PrioriteDemande;
import com.hotelservicesbackend.entity.enums.Role;
import com.hotelservicesbackend.entity.enums.StatutDemande;
import com.hotelservicesbackend.entity.enums.StatutReservation;
import com.hotelservicesbackend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StaffService {

    private final DemandeRepository demandeRepository;
    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ServiceRepository serviceRepository;
    private final ClientRepository clientRepository;
    private final ReservationRepository reservationRepository;
    private final ChambreRepository chambreRepository;
    private final ReceptionnisteRepository receptionnisteRepository;
    private final AffectationRepository affectationRepository;
    private final DepartementRepository departementRepository;

    public StaffService(
            DemandeRepository demandeRepository,
            EmployeRepository employeRepository,
            UtilisateurRepository utilisateurRepository,
            ServiceRepository serviceRepository,
            ClientRepository clientRepository,
            ReservationRepository reservationRepository,
            ChambreRepository chambreRepository,
            ReceptionnisteRepository receptionnisteRepository,
            AffectationRepository affectationRepository,
            DepartementRepository departementRepository
    ) {
        this.demandeRepository = demandeRepository;
        this.employeRepository = employeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.serviceRepository = serviceRepository;
        this.clientRepository = clientRepository;
        this.reservationRepository = reservationRepository;
        this.chambreRepository = chambreRepository;
        this.receptionnisteRepository = receptionnisteRepository;
        this.affectationRepository = affectationRepository;
        this.departementRepository = departementRepository;
    }

    private Employe getEmployeByEmail(String email) {
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé: " + email));
        
        Optional<Employe> empOpt = employeRepository.findById(u.getIdUtilisateur());
        if (empOpt.isPresent()) {
            return empOpt.get();
        }

        // Si l'utilisateur n'est pas encore dans la table employe (ex: admin)
        Employe fallback = new Employe();
        fallback.setIdUtilisateur(u.getIdUtilisateur());
        fallback.setNom(u.getNom());
        fallback.setPrenom(u.getPrenom());
        fallback.setEmail(u.getEmail());
        fallback.setPoste("Superviseur");
        departementRepository.findAll().stream().findFirst().ifPresent(fallback::setDepartement);
        return fallback;
    }

    // =========================================================================
    // 1. ESPACE EMPLOYÉ DE TERRAIN
    // =========================================================================

    public List<StaffDemandeResponse> getEmployeeTasks(String email) {
        Employe employe = getEmployeByEmail(email);
        List<Demande> list = demandeRepository.findByEmployeAssigne_IdUtilisateur(employe.getIdUtilisateur());
        return list.stream().map(this::mapToStaffDemandeResponse).collect(Collectors.toList());
    }

    public List<StaffDemandeResponse> getAvailablePoolForEmployee(String email) {
        Employe employe = getEmployeByEmail(email);
        if (employe.getDepartement() == null) {
            return Collections.emptyList();
        }
        Long deptId = employe.getDepartement().getIdDepartement();
        List<Demande> pool = demandeRepository
                .findByService_Departement_IdDepartementAndEmployeAssigneIsNullAndService_NecessiteSupervisionFalse(deptId);

        return pool.stream()
                .filter(d -> d.getStatut() == StatutDemande.EN_ATTENTE)
                .map(this::mapToStaffDemandeResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffDemandeResponse claimTask(Long idDemande, String email) {
        Employe employe = getEmployeByEmail(email);

        // V?rification des rôles : Seul un employé de terrain (ou admin) peut auto-prendre une tâche simple
        if (employe.getRole() != Role.EMPLOYE && employe.getRole() != Role.STAFF && employe.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul un employé de terrain (rôle EMPLOYE) peut auto-prendre en charge une tâche.");
        }

        Demande demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable: " + idDemande));

        if (demande.getService() != null && demande.getService().isNecessiteSupervision()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette tâche nécessite l'affectation formelle par un superviseur (Personnel).");
        }

        if (demande.getEmployeAssigne() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette tâche a déjà été prise par un autre employé.");
        }

        demande.setEmployeAssigne(employe);
        demande.setStatut(StatutDemande.EN_COURS);
        demande.setDatePriseEnCharge(LocalDateTime.now());
        demande.setDateModification(LocalDateTime.now());

        Demande saved = demandeRepository.save(demande);
        return mapToStaffDemandeResponse(saved);
    }

    @Transactional
    public StaffDemandeResponse completeTask(Long idDemande, String email) {
        Employe employe = getEmployeByEmail(email);
        Demande demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable: " + idDemande));

        // V?rification des rôles : Seul l'employé assigné, un superviseur (Personnel) ou un admin peut clore la tâche
        boolean isAssignee = demande.getEmployeAssigne() != null && demande.getEmployeAssigne().getIdUtilisateur().equals(employe.getIdUtilisateur());
        boolean isAdmin = employe.getRole() == Role.ADMIN;
        boolean isSupervisor = employe.getRole() == Role.PERSONNEL;

        if (!isAssignee && !isAdmin && !isSupervisor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action refusée : vous n'êtes pas l'employé assigné à cette tâche.");
        }

        demande.setStatut(StatutDemande.TERMINEE);
        demande.setDateResolution(LocalDateTime.now());
        demande.setDateCloture(LocalDateTime.now());
        demande.setDateModification(LocalDateTime.now());

        if (demande.getDatePriseEnCharge() != null) {
            long minutes = Duration.between(demande.getDatePriseEnCharge(), LocalDateTime.now()).toMinutes();
            demande.setTempsTraitementReel((int) minutes);
        } else if (demande.getDateCreation() != null) {
            long minutes = Duration.between(demande.getDateCreation(), LocalDateTime.now()).toMinutes();
            demande.setTempsTraitementReel((int) minutes);
        }

        Demande saved = demandeRepository.save(demande);
        return mapToStaffDemandeResponse(saved);
    }

    // =========================================================================
    // 2. ESPACE PERSONNEL (SUPERVISEUR DE DÉPARTEMENT)
    // =========================================================================

    public List<StaffDemandeResponse> getDepartmentTasksForSupervisor(String email) {
        Utilisateur u = utilisateurRepository.findByEmail(email).orElse(null);
        boolean isAdmin = u != null && u.getRole() == Role.ADMIN;

        Employe supervisor = getEmployeByEmail(email);
        Departement dept = supervisor.getDepartement();

        List<Demande> list;
        if (isAdmin) {
            list = demandeRepository.findAll();
        } else if (dept != null) {
            list = demandeRepository.findByService_Departement_IdDepartement(dept.getIdDepartement());
            // Si aucune tâche pour ce département précis, afficher les tâches non assignées nécessitant supervision
            if (list.isEmpty()) {
                list = demandeRepository.findAll().stream()
                        .filter(d -> d.getService() != null && d.getService().isNecessiteSupervision())
                        .collect(Collectors.toList());
            }
        } else {
            list = demandeRepository.findAll().stream()
                    .filter(d -> d.getService() != null && d.getService().isNecessiteSupervision())
                    .collect(Collectors.toList());
        }

        return list.stream().map(this::mapToStaffDemandeResponse).collect(Collectors.toList());
    }

    public List<StaffMemberResponse> getDepartmentTeamForSupervisor(String email) {
        Utilisateur u = utilisateurRepository.findByEmail(email).orElse(null);
        boolean isAdmin = u != null && u.getRole() == Role.ADMIN;

        Employe supervisor = getEmployeByEmail(email);
        Departement dept = supervisor.getDepartement();
        Long deptId = dept != null ? dept.getIdDepartement() : null;

        List<Employe> team;
        if (isAdmin || deptId == null) {
            team = employeRepository.findAll();
        } else {
            team = employeRepository.findByDepartementIdDepartement(deptId);
            // S'il n'y a pas d'agents subordonnés dans ce département, récupérer tous les employés pour permettre l'affectation
            if (team.isEmpty() || team.stream().allMatch(e -> e.getIdUtilisateur().equals(supervisor.getIdUtilisateur()))) {
                team = employeRepository.findAll();
            }
        }

        List<StatutDemande> activeStatuses = List.of(StatutDemande.EN_COURS, StatutDemande.PRISE_EN_CHARGE);

        // FILTRAGE STRICT DES ROLES :
        // Un superviseur (Personnel) n'affecte des tâches qu'à des EMPLOYÉS DE TERRAIN (Role EMPLOYE ou STAFF).
        // On exclut les autres superviseurs (Personnel), réceptionnistes, admins et clients.
        return team.stream()
                .filter(emp -> !emp.getIdUtilisateur().equals(supervisor.getIdUtilisateur()))
                .filter(emp -> emp.getRole() == Role.EMPLOYE || emp.getRole() == Role.STAFF)
                .map(emp -> {
                    long inProgress = demandeRepository.countByEmployeAssigne_IdUtilisateurAndStatutIn(emp.getIdUtilisateur(), activeStatuses);
                    String deptNom = emp.getDepartement() != null ? emp.getDepartement().getNom() : (dept != null ? dept.getNom() : "Général");
                    return new StaffMemberResponse(
                            emp.getIdUtilisateur(),
                            emp.getNom(),
                            emp.getPrenom(),
                            emp.getEmail(),
                            emp.getTel(),
                            emp.getPoste() != null ? emp.getPoste() : "Agent",
                            emp.getDepartement() != null ? emp.getDepartement().getIdDepartement() : deptId,
                            deptNom,
                            inProgress == 0,
                            inProgress
                    );
                }).collect(Collectors.toList());
    }

    @Transactional
    public StaffDemandeResponse assignTask(Long idDemande, Long idEmploye, String supervisorEmail) {
        // Vérification du rôle de l'assignateur (doit être PERSONNEL ou ADMIN)
        Utilisateur supervisorUser = utilisateurRepository.findByEmail(supervisorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Superviseur introuvable"));
        if (supervisorUser.getRole() != Role.PERSONNEL && supervisorUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul un superviseur (rôle PERSONNEL) ou un administrateur peut affecter des tâches.");
        }

        Demande demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable: " + idDemande));

        Employe assignee = employeRepository.findById(idEmploye)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé introuvable: " + idEmploye));

        // Vérification du rôle de l'assigné : DOIT être un employé de terrain (EMPLOYE ou STAFF)
        if (assignee.getRole() != Role.EMPLOYE && assignee.getRole() != Role.STAFF) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sélection invalide : seul un employé de terrain (rôle EMPLOYE) peut être assigné à une tâche.");
        }

        demande.setEmployeAssigne(assignee);
        demande.setStatut(StatutDemande.EN_COURS);
        demande.setDatePriseEnCharge(LocalDateTime.now());
        demande.setDateModification(LocalDateTime.now());

        Demande saved = demandeRepository.save(demande);
        return mapToStaffDemandeResponse(saved);
    }

    public List<StaffDemandeResponse> getSupervisorAlerts(String email) {
        return getDepartmentTasksForSupervisor(email).stream()
                .filter(StaffDemandeResponse::enRetard)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // 3. ESPACE RÉCEPTIONNISTE (TOUR DE CONTRÔLE & SUPPORT CLIENT)
    // =========================================================================

    public ReceptionOverviewResponse getReceptionOverview() {
        List<Demande> all = demandeRepository.findAll();
        long total = all.size();
        long enAttente = all.stream().filter(d -> d.getStatut() == StatutDemande.EN_ATTENTE).count();
        long enCours = all.stream().filter(d -> d.getStatut() == StatutDemande.EN_COURS || d.getStatut() == StatutDemande.PRISE_EN_CHARGE).count();
        long terminees = all.stream().filter(d -> d.getStatut() == StatutDemande.TERMINEE).count();

        LocalDateTime now = LocalDateTime.now();
        long alertes = all.stream().filter(d ->
                d.getStatut() != StatutDemande.TERMINEE &&
                d.getStatut() != StatutDemande.REJETEE &&
                d.getStatut() != StatutDemande.ANNULEE &&
                d.getDateCreation() != null &&
                d.getDateCreation().plusMinutes(d.getTempsTraitementEstime()).isBefore(now)
        ).count();

        long chambresOccupees = reservationRepository.findByStatut(StatutReservation.EN_COURS).size();

        return new ReceptionOverviewResponse(total, enAttente, enCours, terminees, alertes, chambresOccupees);
    }

    public List<StaffDemandeResponse> getReceptionAllDemandes(String query, Long idDepartement, StatutDemande statut) {
        List<Demande> list = demandeRepository.findAll();
        return list.stream()
                .filter(d -> {
                    if (idDepartement != null && (d.getService() == null || d.getService().getDepartement() == null || !d.getService().getDepartement().getIdDepartement().equals(idDepartement))) {
                        return false;
                    }
                    if (statut != null && d.getStatut() != statut) {
                        return false;
                    }
                    if (query != null && !query.trim().isEmpty()) {
                        String q = query.trim().toLowerCase();
                        boolean matchRef = d.getReferenceDemande() != null && d.getReferenceDemande().toLowerCase().contains(q);
                        boolean matchService = d.getService() != null && d.getService().getNom().toLowerCase().contains(q);
                        boolean matchClient = d.getClient() != null && (d.getClient().getNom().toLowerCase().contains(q) || d.getClient().getPrenom().toLowerCase().contains(q));
                        return matchRef || matchService || matchClient;
                    }
                    return true;
                })
                .map(this::mapToStaffDemandeResponse)
                .collect(Collectors.toList());
    }

    public List<RoomGuestOption> getRoomGuestOptions() {
        List<Reservation> activeReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStatut() == StatutReservation.EN_COURS || r.getStatut() == StatutReservation.CONFIRMEE)
                .toList();

        List<RoomGuestOption> options = new ArrayList<>();
        for (Reservation res : activeReservations) {
            Client c = res.getClient();
            Chambre ch = res.getChambre();
            if (c != null && ch != null) {
                options.add(new RoomGuestOption(
                        c.getIdUtilisateur(),
                        c.getNom(),
                        c.getPrenom(),
                        c.getEmail(),
                        ch.getIdChambre(),
                        ch.getNumChambre(),
                        ch.getTypeChambre(),
                        res.getIdReservation()
                ));
            }
        }
        return options;
    }

    @Transactional
    public StaffDemandeResponse receptionCreateDemand(ReceptionCreateRequest request, String receptionistEmail) {
        Client client = clientRepository.findById(request.idClient())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable: " + request.idClient()));

        com.hotelservicesbackend.entity.Service service = serviceRepository.findById(request.idService())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service introuvable: " + request.idService()));

        Utilisateur recpUser = utilisateurRepository.findByEmail(receptionistEmail).orElse(null);
        Receptionniste recp = recpUser != null ? receptionnisteRepository.findById(recpUser.getIdUtilisateur()).orElse(null) : null;

        Demande demande = new Demande();
        demande.setClient(client);
        demande.setService(service);
        demande.setReceptionniste(recp);
        demande.setDateCreation(LocalDateTime.now());
        demande.setStatut(StatutDemande.EN_ATTENTE);
        demande.setPriorite(request.priorite() != null ? request.priorite() : PrioriteDemande.NORMALE);
        demande.setTempsTraitementEstime(service.getDureeEstimee());
        demande.setCommentairesClient(request.commentairesClient());
        demande.setReferenceDemande("DEM-" + System.currentTimeMillis());

        Demande saved = demandeRepository.save(demande);
        return mapToStaffDemandeResponse(saved);
    }

    // =========================================================================
    // MAPPER & CALCUL SLA / RETARD
    // =========================================================================

    private StaffDemandeResponse mapToStaffDemandeResponse(Demande d) {
        String numChambre = "201";
        if (d.getClient() != null) {
            List<Reservation> resList = reservationRepository.findByClient_IdUtilisateur(d.getClient().getIdUtilisateur());
            if (!resList.isEmpty() && resList.get(0).getChambre() != null) {
                numChambre = resList.get(0).getChambre().getNumChambre();
            }
        }

        LocalDateTime creation = d.getDateCreation() != null ? d.getDateCreation() : LocalDateTime.now();
        int dureeEstimee = d.getTempsTraitementEstime() > 0 ? d.getTempsTraitementEstime() : 30;
        LocalDateTime deadline = creation.plusMinutes(dureeEstimee);
        LocalDateTime now = LocalDateTime.now();

        boolean isActive = d.getStatut() != StatutDemande.TERMINEE &&
                           d.getStatut() != StatutDemande.REJETEE &&
                           d.getStatut() != StatutDemande.ANNULEE;

        boolean enRetard = isActive && now.isAfter(deadline);
        long retardMinutes = enRetard ? Duration.between(deadline, now).toMinutes() : 0;

        com.hotelservicesbackend.entity.Service s = d.getService();
        Departement dept = s != null ? s.getDepartement() : null;
        Employe emp = d.getEmployeAssigne();
        Client c = d.getClient();

        return new StaffDemandeResponse(
                d.getIdDemande(),
                d.getReferenceDemande(),
                s != null ? s.getIdService() : null,
                s != null ? s.getNom() : "Service général",
                s != null ? s.getCategorie() : "Room Service",
                s != null ? s.getDureeEstimee() : 30,
                s != null && s.isNecessiteSupervision(),
                dept != null ? dept.getIdDepartement() : null,
                dept != null ? dept.getNom() : "Général",
                c != null ? c.getIdUtilisateur() : null,
                c != null ? c.getNom() : "Client",
                c != null ? c.getPrenom() : "Hôtel",
                c != null ? c.getEmail() : "",
                numChambre,
                d.getStatut(),
                d.getPriorite(),
                d.getDateCreation(),
                d.getDatePriseEnCharge(),
                d.getDateResolution(),
                dureeEstimee,
                d.getTempsTraitementReel(),
                emp != null ? emp.getIdUtilisateur() : null,
                emp != null ? (emp.getPrenom() + " " + emp.getNom()) : null,
                emp != null ? emp.getPoste() : null,
                d.getCommentairesClient(),
                enRetard,
                retardMinutes
        );
    }
}

