import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { DemandeManagementComponent } from '../demandes/demande-management.component';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [DemandeManagementComponent],
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.scss',
})
export class ClientDashboardComponent {
  constructor(private auth: AuthService, private router: Router) {}
  get currentUser() { return this.auth.currentUser; }
  logout(): void { this.auth.logout(); this.router.navigate(['/login']); }
}
