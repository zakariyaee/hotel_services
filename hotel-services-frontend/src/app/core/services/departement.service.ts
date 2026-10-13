import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Departement } from '../models/departement.model';

@Injectable({ providedIn: 'root' })
export class DepartementService {
  private readonly apiUrl = `${environment.apiUrl}/departements`;

  constructor(private http: HttpClient) {}
  getAll(): Observable<Departement[]> { return this.http.get<Departement[]>(this.apiUrl); }
  create(value: Partial<Departement>): Observable<Departement> { return this.http.post<Departement>(this.apiUrl, value); }
  update(id: number, value: Partial<Departement>): Observable<Departement> { return this.http.put<Departement>(`${this.apiUrl}/${id}`, value); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}
