import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ServiceType } from '../models/service-type.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServiceTypeService {
  private apiUrl = `${environment.apiUrl}/service-types`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ServiceType[]> {
    return this.http.get<ServiceType[]>(this.apiUrl);
  }
}
