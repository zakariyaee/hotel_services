import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from './core/services/auth.service';

@Component({ selector: 'app-root', standalone: true, imports: [FormsModule], templateUrl: './app.html', styleUrl: './app.scss' })
export class App {
  username = '412';
  password = '123456';
  isConnected = signal(false);
  connectedUser = signal('');
  errorMessage = signal('');
  isLoading = signal(false);
  constructor(private authService: AuthService) {}
  login(): void {
    this.errorMessage.set('');
    this.isLoading.set(true);
    this.authService.login(this.username, this.password).subscribe({
      next: user => {
        this.connectedUser.set(user.username);
        this.isConnected.set(true);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Nom d’utilisateur ou code d’accès incorrect.');
        this.isLoading.set(false);
      },
    });
  }
  logout(): void {
    this.isConnected.set(false);
    this.connectedUser.set('');
  }
}
