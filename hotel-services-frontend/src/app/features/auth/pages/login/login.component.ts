import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  email = '';
  motDePasse = '';
  readonly errorMessage = signal('');
  readonly isLoading = signal(false);

  constructor(private authService: AuthService, private router: Router) {
    // Si déjà connecté, rediriger directement
    if (this.authService.isLoggedIn()) {
      const role = this.authService.getRole();
      this.router.navigate([this.routeForRole(role ?? '')]);
    }
  }

  login(): void {
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
