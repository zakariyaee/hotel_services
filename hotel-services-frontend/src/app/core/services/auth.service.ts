import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginResponse } from '../models/login-response.model';
import { LoginRequest } from '../models/login-request.model';
import { GuestAccessResponse } from '../models/guest-access-response.model';
import { GuestAccessRequest } from '../models/guest-access-request.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8090/api/auth';
  private readonly storageKey = 'hotel_services_user';
  private readonly tokenKey = 'hotel_services_token';
  private readonly guestInfoKey = 'hotel_services_guest_info';

  private getInitialUser(): LoginResponse | null {
    try {
      const stored = localStorage.getItem(this.storageKey);
      if (stored) return JSON.parse(stored);
    } catch {
      // Ignore storage errors
    }
    return null;
  }

  private getInitialGuestInfo(): GuestAccessResponse | null {
    try {
      const stored = localStorage.getItem(this.guestInfoKey);
      if (stored) return JSON.parse(stored);
    } catch {
      // Ignore storage errors
    }
    return null;
  }

  readonly currentUser = signal<LoginResponse | null>(this.getInitialUser());
  readonly currentGuestInfo = signal<GuestAccessResponse | null>(this.getInitialGuestInfo());

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
            localStorage.removeItem(this.guestInfoKey);
          } catch {
            // Ignore storage errors
          }
          this.currentGuestInfo.set(null);
        })
      );
  }

  guestLogin(accessCode: string): Observable<GuestAccessResponse> {
    const request: GuestAccessRequest = { accessCode };
    return this.http
      .post<GuestAccessResponse>(`${this.apiUrl}/guest-access`, request)
      .pipe(
        tap(guest => {
          const user: LoginResponse = {
            idUtilisateur: guest.idUtilisateur,
            email: guest.email,
            role: guest.role,
            token: guest.token,
          };
          this.currentUser.set(user);
          this.currentGuestInfo.set(guest);
          try {
            localStorage.setItem(this.storageKey, JSON.stringify(user));
            localStorage.setItem(this.tokenKey, guest.token);
            localStorage.setItem(this.guestInfoKey, JSON.stringify(guest));
          } catch {
            // Ignore storage errors
          }
        })
      );
  }

  logout(): void {
    this.currentUser.set(null);
    this.currentGuestInfo.set(null);
    try {
      localStorage.removeItem(this.storageKey);
      localStorage.removeItem(this.tokenKey);
      localStorage.removeItem(this.guestInfoKey);
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
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  getRole(): string | null {
    return this.currentUser()?.role ?? null;
  }

  getRoomNumber(): string {
    return this.currentGuestInfo()?.numChambre ?? '201';
  }

  getRoomType(): string {
    return this.currentGuestInfo()?.typeChambre ?? 'Suite Royale Prestige';
  }

  getGuestFullName(): string {
    const info = this.currentGuestInfo();
    if (info?.prenom || info?.nom) {
      return `${info.prenom ?? ''} ${info.nom ?? ''}`.trim();
    }
    return 'Zakaria Allouch';
  }
}
