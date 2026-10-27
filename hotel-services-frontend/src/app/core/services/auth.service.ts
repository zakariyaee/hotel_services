import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginResponse } from '../models/login-response.model';
import { LoginRequest } from '../models/login-request.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8090/api/auth';
  private readonly storageKey = 'hotel_services_user';

  private getInitialUser(): LoginResponse {
    try {
      const stored = localStorage.getItem(this.storageKey);
      if (stored) return JSON.parse(stored);
    } catch (e) {
      // Ignore storage errors
    }
    // Default client (Camille / Zakaria id 1)
    return {
      idUtilisateur: 1,
      email: 'camille@hotel.com',
      role: 'CLIENT',
    };
  }

  readonly currentUser = signal<LoginResponse | null>(this.getInitialUser());

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(user => {
          this.currentUser.set(user);
          try {
            localStorage.setItem(this.storageKey, JSON.stringify(user));
          } catch (e) {}
        })
      );
  }

  logout(): void {
    this.currentUser.set(null);
    try {
      localStorage.removeItem(this.storageKey);
    } catch (e) {}
  }
}

