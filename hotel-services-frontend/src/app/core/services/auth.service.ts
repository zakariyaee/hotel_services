import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginResponse } from '../models/login-response.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8090/api/auth';
  readonly currentUser = signal<LoginResponse | null>(null);
  constructor(private http: HttpClient) {}
  login(username: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, { username, password })
      .pipe(tap(user => this.currentUser.set(user)));
  }

  logout(): void { this.currentUser.set(null); }
}
