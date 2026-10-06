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
  username = '412';
  password = '123456';
  readonly errorMessage = signal('');
  readonly isLoading = signal(false);

  constructor(private authService: AuthService, private router: Router) {}

  login(): void {
    this.errorMessage.set('');
    this.isLoading.set(true);
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/connected']);
      },
      error: () => {
        this.errorMessage.set('Nom d’utilisateur ou code d’accès incorrect.');
        this.isLoading.set(false);
      },
    });
  }
}
