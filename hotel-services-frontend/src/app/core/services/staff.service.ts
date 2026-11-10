import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  StaffDemande,
  StaffMember,
  ReceptionOverview,
  RoomGuestOption,
  ReceptionCreateRequest,
  StaffProfile
} from '../models/staff.model';

@Injectable({
  providedIn: 'root',
})
export class StaffService {
  private readonly baseUrl = 'http://localhost:8090/api/staff';

  constructor(private http: HttpClient) {}

  // =========================================================================
  // 1. EMPLOYÉ DE TERRAIN
  // =========================================================================
  getMyTasks(): Observable<StaffDemande[]> {
    return this.http.get<StaffDemande[]>(`${this.baseUrl}/employe/my-tasks`);
  }

  getEmployeeProfile(): Observable<StaffProfile> {
    return this.http.get<StaffProfile>(`${this.baseUrl}/employe/profile`);
  }

  getAvailablePool(): Observable<StaffDemande[]> {
    return this.http.get<StaffDemande[]>(`${this.baseUrl}/employe/available-pool`);
  }

  claimTask(idDemande: number): Observable<StaffDemande> {
    return this.http.post<StaffDemande>(`${this.baseUrl}/employe/tasks/${idDemande}/claim`, {});
  }

  completeTask(idDemande: number): Observable<StaffDemande> {
    return this.http.post<StaffDemande>(`${this.baseUrl}/employe/tasks/${idDemande}/complete`, {});
  }

  // =========================================================================
  // 2. PERSONNEL (SUPERVISEUR)
  // =========================================================================
  getDepartmentTasks(): Observable<StaffDemande[]> {
    return this.http.get<StaffDemande[]>(`${this.baseUrl}/personnel/department-tasks`);
  }

  getDepartmentTeam(): Observable<StaffMember[]> {
    return this.http.get<StaffMember[]>(`${this.baseUrl}/personnel/team`);
  }

  assignTask(idDemande: number, idEmploye: number): Observable<StaffDemande> {
    return this.http.post<StaffDemande>(`${this.baseUrl}/personnel/tasks/${idDemande}/assign`, { idEmploye });
  }

  getDepartmentAlerts(): Observable<StaffDemande[]> {
    return this.http.get<StaffDemande[]>(`${this.baseUrl}/personnel/alerts`);
  }

  // =========================================================================
  // 3. RÉCEPTIONNISTE
  // =========================================================================
  getReceptionOverview(): Observable<ReceptionOverview> {
    return this.http.get<ReceptionOverview>(`${this.baseUrl}/reception/overview`);
  }

  getReceptionDemandes(query?: string, idDepartement?: number, statut?: string): Observable<StaffDemande[]> {
    let params = new HttpParams();
    if (query) params = params.set('query', query);
    if (idDepartement) params = params.set('idDepartement', idDepartement.toString());
    if (statut) params = params.set('statut', statut);
    return this.http.get<StaffDemande[]>(`${this.baseUrl}/reception/demandes`, { params });
  }

  getRoomGuests(): Observable<RoomGuestOption[]> {
    return this.http.get<RoomGuestOption[]>(`${this.baseUrl}/reception/room-guests`);
  }

  receptionCreateDemand(request: ReceptionCreateRequest): Observable<StaffDemande> {
    return this.http.post<StaffDemande>(`${this.baseUrl}/reception/create-demand`, request);
  }

  receptionRejectDemand(idDemande: number, motifRejet?: string): Observable<StaffDemande> {
    return this.http.post<StaffDemande>(`${this.baseUrl}/reception/demandes/${idDemande}/reject`, { motifRejet });
  }

  receptionAssignDemand(idDemande: number, idEmploye: number): Observable<StaffDemande> {
    return this.http.post<StaffDemande>(`${this.baseUrl}/reception/demandes/${idDemande}/assign`, { idEmploye });
  }
}

