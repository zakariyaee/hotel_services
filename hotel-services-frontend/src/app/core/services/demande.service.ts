import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Demande, CreateDemandeDto } from '../models/demande.model';

@Injectable({ providedIn: 'root' })
export class DemandeService {
  private readonly apiUrl = `${environment.apiUrl}/demandes`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Demande[]> {
    return this.http.get<Demande[]>(this.apiUrl);
  }

  getById(id: number): Observable<Demande> {
    return this.http.get<Demande>(`${this.apiUrl}/${id}`);
  }

  create(dto: CreateDemandeDto): Observable<Demande> {
    return this.http.post<Demande>(this.apiUrl, dto);
  }

  getByClient(clientId: number): Observable<Demande[]> {
    return this.http.get<Demande[]>(`${this.apiUrl}/client/${clientId}`);
  }

  update(id: number, data: Partial<Demande>): Observable<Demande> {
    return this.http.put<Demande>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
