import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  // Tabs: 'guest' (Code séjour) ou 'staff' (Personnel & Direction)
  activeTab: 'guest' | 'staff' = 'guest';

  // Guest access state
  accessCode = 'RES-2026-001';

  // Staff login state
  email = '';
  motDePasse = '';

  readonly errorMessage = signal('');
  readonly isLoading = signal(false);

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.isLoggedIn()) {
      const role = this.authService.getRole();
      this.router.navigate([this.routeForRole(role ?? '')]);
    }
  }

  setTab(tab: 'guest' | 'staff'): void {
    this.activeTab = tab;
    this.errorMessage.set('');
  }

  fillGuestDemo(code: string): void {
    this.accessCode = code;
  }

  fillStaffDemo(email: string, pass: string): void {
    this.email = email;
    this.motDePasse = pass;
  }

  loginGuest(): void {
    this.errorMessage.set('');
    const code = this.accessCode.trim();
    if (!code) {
      this.errorMessage.set('Veuillez saisir votre code de réservation ou pass séjour.');
      return;
    }

    this.isLoading.set(true);
    this.authService.guestLogin(code).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/client']);
      },
      error: err => {
        this.isLoading.set(false);
        const msg = err.error?.message ?? "Code d'accès invalide ou réservation non active.";
        this.errorMessage.set(msg);
      },
    });
  }

  loginStaff(): void {
    this.errorMessage.set('');
    if (!this.email.trim() || !this.motDePasse.trim()) {
      this.errorMessage.set('Veuillez renseigner votre email et mot de passe.');
      return;
    }

    this.isLoading.set(true);
    this.authService.login({ email: this.email, motDePasse: this.motDePasse }).subscribe({
      next: user => {
        this.isLoading.set(false);
        this.router.navigate([this.routeForRole(user.role)]);
      },
      error: err => {
        this.isLoading.set(false);
        const msg = err.error?.message ?? 'Identifiant ou mot de passe incorrect.';
        this.errorMessage.set(msg);
      },
    });
  }

  private routeForRole(role: string): string {
    switch (role.toUpperCase()) {
      case 'ADMIN': return '/admin';
      case 'STAFF':
      case 'RECEPTIONNISTE':
      case 'RECEPTIONIST': return '/staff';
      default: return '/client';
    }
  }
}
