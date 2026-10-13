import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Departement } from '../models/departement.model';

@Injectable({ providedIn: 'root' })
export class DepartementService {
  private readonly apiUrl = `${environment.apiUrl}/departements`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Departement[]> {
    return this.http.get<Departement[]>(this.apiUrl);
  }

  getById(id: number): Observable<Departement> {
    return this.http.get<Departement>(`${this.apiUrl}/${id}`);
  }
}
