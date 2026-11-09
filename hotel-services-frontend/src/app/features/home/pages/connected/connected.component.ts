import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { AdminCrudComponent } from '../../../admin/admin-crud.component';
import { ClientDashboardComponent } from '../../../client/client-dashboard.component';
import { NotificationWebSocketService } from '../../../../core/services/notification-websocket.service';
import { NotificationBellComponent } from '../../../../shared/notification-bell/notification-bell.component';

@Component({
  selector: 'app-connected',
  standalone: true,
  imports: [AdminCrudComponent, ClientDashboardComponent, NotificationBellComponent],
  templateUrl: './connected.component.html',
  styleUrl: './connected.component.scss',
})
export class ConnectedComponent implements OnInit, OnDestroy {
  constructor(
    private authService: AuthService,
    private router: Router,
    public wsNotif: NotificationWebSocketService
  ) {}

  ngOnInit(): void {
    const token = this.authService.getToken();
    if (token) this.wsNotif.connect(token);
  }

  ngOnDestroy(): void {
    this.wsNotif.disconnect();
  }

  get currentUser() {
    return this.authService.currentUser;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
