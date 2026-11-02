import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminUtilisateur, CreateAdminUtilisateurDto } from '../models/admin-utilisateur.model';

@Injectable({
  providedIn: 'root',
})
export class AdminUtilisateurService {
  private readonly apiUrl = 'http://localhost:8090/api/admin/utilisateurs';

  constructor(private http: HttpClient) {}

  getAll(): Observable<AdminUtilisateur[]> {
    return this.http.get<AdminUtilisateur[]>(this.apiUrl);
  }

  getById(id: number): Observable<AdminUtilisateur> {
    return this.http.get<AdminUtilisateur>(`${this.apiUrl}/${id}`);
  }

  create(dto: CreateAdminUtilisateurDto): Observable<AdminUtilisateur> {
    return this.http.post<AdminUtilisateur>(this.apiUrl, dto);
  }

  update(id: number, dto: CreateAdminUtilisateurDto): Observable<AdminUtilisateur> {
    return this.http.put<AdminUtilisateur>(`${this.apiUrl}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  assignDepartement(idUtilisateur: number, idDepartement: number, poste?: string): Observable<AdminUtilisateur> {
    const url = poste
      ? `${this.apiUrl}/${idUtilisateur}/assign-departement/${idDepartement}?poste=${encodeURIComponent(poste)}`
      : `${this.apiUrl}/${idUtilisateur}/assign-departement/${idDepartement}`;
    return this.http.post<AdminUtilisateur>(url, {});
  }

  unassignDepartement(idUtilisateur: number): Observable<AdminUtilisateur> {
    return this.http.post<AdminUtilisateur>(`${this.apiUrl}/${idUtilisateur}/unassign-departement`, {});
  }
}

