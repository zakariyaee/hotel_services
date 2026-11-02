import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
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
  errorMessage = signal('');
  isLoading = signal(false);
  isAutoLogin = signal(false);

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // QR Code auto-login: /login?email=...&code=...&auto=true
    this.route.queryParams.subscribe(params => {
      const emailParam = params['email'];
      const codeParam = params['code'];
      const autoParam = params['auto'];

      if (autoParam === 'true' && emailParam && codeParam) {
        this.isAutoLogin.set(true);
        this.email = emailParam;
        this.motDePasse = codeParam;
        this.performLogin(emailParam, codeParam);
      }
    });
  }

  private performLogin(email: string, password: string): void {
    this.isLoading.set(true);
    this.authService.login({ email: email.trim(), motDePasse: password.trim() }).subscribe({
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

  login(): void {
    this.errorMessage.set('');
    if (!this.email.trim() || !this.motDePasse.trim()) {
      this.errorMessage.set('Veuillez renseigner votre email et mot de passe.');
      return;
    }
    this.performLogin(this.email, this.motDePasse);
  }

  fillDemo(email: string, pass: string): void {
    this.email = email;
    this.motDePasse = pass;
    this.errorMessage.set('');
  }

  private routeForRole(role: string): string {
    switch (role.toUpperCase()) {
      case 'ADMIN': return '/admin';
      case 'PERSONNEL': return '/staff/personnel';
      case 'RECEPTIONNISTE': return '/staff/reception';
      case 'EMPLOYE':
      case 'STAFF': return '/staff/employe';
      default: return '/client';
    }
  }
}
