import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent implements OnInit {
  email = '';
  motDePasse = '';

  readonly errorMessage = signal('');
  readonly isLoading = signal(false);
  readonly isAutoLogin = signal(false);

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    if (this.authService.isLoggedIn()) {
      const role = this.authService.getRole();
      this.router.navigate([this.routeForRole(role ?? '')]);
    }
  }

  ngOnInit(): void {
    // Détection automatique du scan QR Code (paramètres email & code dans l'URL)
    this.route.queryParamMap.subscribe(params => {
      const emailParam = params.get('email');
      const codeParam = params.get('code');
      const autoParam = params.get('auto');

      if (emailParam && codeParam) {
        this.email = emailParam;
        this.motDePasse = codeParam;

        if (autoParam === 'true') {
          this.isAutoLogin.set(true);
          // Auto-connexion immédiate lors du scan QR Code
          this.login();
        }
      }
    });
  }

  login(): void {
    this.errorMessage.set('');
    if (!this.email.trim() || !this.motDePasse.trim()) {
      this.errorMessage.set('Veuillez renseigner votre email et mot de passe.');
      return;
    }

    this.isLoading.set(true);
    this.authService.login({ email: this.email.trim(), motDePasse: this.motDePasse.trim() }).subscribe({
      next: user => {
        this.isLoading.set(false);
        this.isAutoLogin.set(false);
        this.router.navigate([this.routeForRole(user.role)]);
      },
      error: err => {
        this.isLoading.set(false);
        this.isAutoLogin.set(false);
        const msg = err.error?.message ?? 'Identifiant ou mot de passe incorrect.';
        this.errorMessage.set(msg);
      },
    });
  }

  fillDemo(email: string, pass: string): void {
    this.email = email;
    this.motDePasse = pass;
    this.errorMessage.set('');
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
