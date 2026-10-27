import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { AdminCrudComponent } from '../../../admin/admin-crud.component';
import { ClientDashboardComponent } from '../../../client/client-dashboard.component';

@Component({
  selector: 'app-connected',
  standalone: true,
  imports: [AdminCrudComponent, ClientDashboardComponent],
  templateUrl: './connected.component.html',
  styleUrl: './connected.component.scss',
})
export class ConnectedComponent {
  constructor(private authService: AuthService, private router: Router) {}

  get currentUser() {
    return this.authService.currentUser;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
