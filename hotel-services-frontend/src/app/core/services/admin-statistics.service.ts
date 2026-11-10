import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminStatistics } from '../models/admin-statistics.model';

@Injectable({ providedIn: 'root' })
export class AdminStatisticsService {
  private readonly apiUrl = 'http://localhost:8090/api/admin/statistics';

  constructor(private http: HttpClient) {}

  get(): Observable<AdminStatistics> {
    return this.http.get<AdminStatistics>(this.apiUrl);
  }
}
