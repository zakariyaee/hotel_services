package com.hotelservicesbackend.controller;

import com.hotelservicesbackend.dto.staff.*;
import com.hotelservicesbackend.entity.enums.StatutDemande;
import com.hotelservicesbackend.service.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    // =========================================================================
    // 1. ENDPOINTS EMPLOYÉ DE TERRAIN
    // =========================================================================

    @GetMapping("/employe/my-tasks")
    public List<StaffDemandeResponse> getMyTasks(Authentication authentication) {
        return staffService.getEmployeeTasks(authentication.getName());
    }

    @GetMapping("/employe/available-pool")
    public List<StaffDemandeResponse> getAvailablePool(Authentication authentication) {
        return staffService.getAvailablePoolForEmployee(authentication.getName());
    }

    @PostMapping("/employe/tasks/{idDemande}/claim")
    public StaffDemandeResponse claimTask(@PathVariable Long idDemande, Authentication authentication) {
        return staffService.claimTask(idDemande, authentication.getName());
    }

    @PostMapping("/employe/tasks/{idDemande}/complete")
    public StaffDemandeResponse completeTask(@PathVariable Long idDemande, Authentication authentication) {
        return staffService.completeTask(idDemande, authentication.getName());
    }

    // =========================================================================
    // 2. ENDPOINTS PERSONNEL (SUPERVISEUR DE DÉPARTEMENT)
    // =========================================================================

    @GetMapping("/personnel/department-tasks")
    public List<StaffDemandeResponse> getDepartmentTasks(Authentication authentication) {
        return staffService.getDepartmentTasksForSupervisor(authentication.getName());
    }

    @GetMapping("/personnel/team")
    public List<StaffMemberResponse> getDepartmentTeam(Authentication authentication) {
        return staffService.getDepartmentTeamForSupervisor(authentication.getName());
    }

    @PostMapping("/personnel/tasks/{idDemande}/assign")
    public StaffDemandeResponse assignTask(
            @PathVariable Long idDemande,
            @RequestBody AssignTaskRequest request,
            Authentication authentication
    ) {
        return staffService.assignTask(idDemande, request.idEmploye(), authentication.getName());
    }

    @GetMapping("/personnel/alerts")
    public List<StaffDemandeResponse> getSupervisorAlerts(Authentication authentication) {
        return staffService.getSupervisorAlerts(authentication.getName());
    }

    // =========================================================================
    // 3. ENDPOINTS RÉCEPTIONNISTE (TOUR DE CONTRÔLE & SUPPORT CLIENT)
    // =========================================================================

    @GetMapping("/reception/overview")
    public ReceptionOverviewResponse getReceptionOverview() {
        return staffService.getReceptionOverview();
    }

    @GetMapping("/reception/demandes")
    public List<StaffDemandeResponse> getReceptionDemandes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long idDepartement,
            @RequestParam(required = false) StatutDemande statut
    ) {
        return staffService.getReceptionAllDemandes(query, idDepartement, statut);
    }

    @GetMapping("/reception/room-guests")
    public List<RoomGuestOption> getRoomGuests() {
        return staffService.getRoomGuestOptions();
    }

    @PostMapping("/reception/create-demand")
    @ResponseStatus(HttpStatus.CREATED)
    public StaffDemandeResponse receptionCreateDemand(
            @RequestBody ReceptionCreateRequest request,
            Authentication authentication
    ) {
        return staffService.receptionCreateDemand(request, authentication.getName());
    }
}

