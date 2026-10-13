import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Chambre } from '../models/chambre.model';

@Injectable({ providedIn: 'root' })
export class ChambreService {
  private readonly apiUrl = `${environment.apiUrl}/chambres`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Chambre[]> {
    return this.http.get<Chambre[]>(this.apiUrl);
  }

  getById(id: number): Observable<Chambre> {
    return this.http.get<Chambre>(`${this.apiUrl}/${id}`);
  }
}
