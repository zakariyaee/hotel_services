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
  username = 'client1';
  password = 'client123';
  readonly errorMessage = signal('');
  readonly isLoading = signal(false);

  constructor(private authService: AuthService, private router: Router) {}

  login(): void {
    this.errorMessage.set('');
    this.isLoading.set(true);
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: user => {
        this.isLoading.set(false);
        this.router.navigate([this.routeForRole(user.role)]);
      },
      error: () => {
        this.errorMessage.set('Identifiant ou mot de passe incorrect.');
        this.isLoading.set(false);
      },
    });
  }

  private routeForRole(role: string): string {
    switch (role.toUpperCase()) {
      case 'ADMIN': return '/admin';
      case 'STAFF': return '/staff';
      default: return '/client';
    }
  }
}
