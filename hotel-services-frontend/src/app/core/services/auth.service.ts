import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginResponse } from '../models/login-response.model';
import { LoginRequest } from '../models/login-request.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8090/api/auth';
  private readonly storageKey = 'hotel_services_user';
  private readonly tokenKey = 'hotel_services_token';

  private getInitialUser(): LoginResponse | null {
    try {
      const stored = localStorage.getItem(this.storageKey);
      if (stored) return JSON.parse(stored);
    } catch {
      // Ignore storage errors
    }
    return null;
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
            localStorage.setItem(this.tokenKey, user.token);
          } catch {
            // Ignore storage errors
          }
        })
      );
  }

  logout(): void {
    this.currentUser.set(null);
    try {
      localStorage.removeItem(this.storageKey);
      localStorage.removeItem(this.tokenKey);
    } catch {
      // Ignore storage errors
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      // Decode payload (base64) to check expiration
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  getRole(): string | null {
    return this.currentUser()?.role ?? null;
  }
}
