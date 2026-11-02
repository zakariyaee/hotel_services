import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminReservation, CreateReservationDto } from '../models/admin-reservation.model';

@Injectable({ providedIn: 'root' })
export class AdminReservationService {
  private readonly apiUrl = 'http://localhost:8090/api/admin/reservations';

  constructor(private http: HttpClient) {}

  getAll(): Observable<AdminReservation[]> {
    return this.http.get<AdminReservation[]>(this.apiUrl);
  }

  getById(id: number): Observable<AdminReservation> {
    return this.http.get<AdminReservation>(`${this.apiUrl}/${id}`);
  }

  create(dto: CreateReservationDto): Observable<AdminReservation> {
    return this.http.post<AdminReservation>(this.apiUrl, dto);
  }
}

