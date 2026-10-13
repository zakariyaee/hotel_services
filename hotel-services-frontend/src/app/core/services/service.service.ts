import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Service } from '../models/service.model';

@Injectable({ providedIn: 'root' })
export class HotelServiceService {
  private readonly apiUrl = `${environment.apiUrl}/services`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Service[]> {
    return this.http.get<Service[]>(this.apiUrl);
  }

  getById(id: number): Observable<Service> {
    return this.http.get<Service>(`${this.apiUrl}/${id}`);
  }

  create(service: Partial<Service>): Observable<Service> {
    return this.http.post<Service>(this.apiUrl, service);
  }
}
