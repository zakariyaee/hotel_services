import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { StaffService } from '../../../core/services/staff.service';
import { StaffDemande } from '../../../core/models/staff.model';
import { HotelServiceService } from '../../../core/services/service.service';
import { Service } from '../../../core/models/service.model';
import { NotificationWebSocketService } from '../../../core/services/notification-websocket.service';
import { NotificationBellComponent } from '../../../shared/notification-bell/notification-bell.component';

@Component({
  selector: 'app-employe-dashboard',
  standalone: true,
  imports: [CommonModule, NotificationBellComponent],
  templateUrl: './employe-dashboard.component.html',
  styleUrl: './employe-dashboard.component.scss',
})
export class EmployeDashboardComponent implements OnInit, OnDestroy {
  myTasks = signal<StaffDemande[]>([]);
  poolTasks = signal<StaffDemande[]>([]);
  isLoading = signal(true);
  toastMsg = signal('');
  toastError = signal(false);
  activeTab: 'my' | 'pool' = 'my';

  constructor(
    public auth: AuthService,
    private staffApi: StaffService,
    private router: Router,
    public wsNotif: NotificationWebSocketService
  ) {}

  ngOnInit(): void {
    this.loadAll();
    const token = this.auth.getToken();
    if (token) this.wsNotif.connect(token);
  }

  ngOnDestroy(): void {
    this.wsNotif.disconnect();
  }

  loadAll(): void {
    this.isLoading.set(true);
    this.staffApi.getMyTasks().subscribe({
      next: data => { this.myTasks.set(data); this.isLoading.set(false); },
      error: () => this.isLoading.set(false),
    });
    this.staffApi.getAvailablePool().subscribe({
      next: data => this.poolTasks.set(data),
      error: () => {},
    });
  }

  claimTask(id: number): void {
    this.staffApi.claimTask(id).subscribe({
      next: () => { this.showToast('Tâche prise en charge !', false); this.loadAll(); },
      error: (err) => this.showToast(err.error?.message || 'Erreur lors de la prise en charge.', true),
    });
  }

  completeTask(id: number): void {
    this.staffApi.completeTask(id).subscribe({
      next: () => { this.showToast('Tâche marquée comme terminée ✓', false); this.loadAll(); },
      error: () => this.showToast('Impossible de marquer la tâche comme terminée.', true),
    });
  }

  get currentUser() { return this.auth.currentUser(); }

  get completedCount(): number {
    return this.myTasks().filter(t => t.statut === 'TERMINEE').length;
  }

  get activeCount(): number {
    return this.myTasks().filter(t => t.statut === 'EN_COURS' || t.statut === 'PRISE_EN_CHARGE').length;
  }

  showToast(msg: string, isError: boolean): void {
    this.toastMsg.set(msg);
    this.toastError.set(isError);
    setTimeout(() => this.toastMsg.set(''), 4000);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  priorityClass(p: string): string {
    switch (p) {
      case 'URGENTE': return 'prio-urgent';
      case 'HAUTE': return 'prio-high';
      case 'NORMALE': return 'prio-normal';
      default: return 'prio-low';
    }
  }

  statusLabel(s: string): string {
    switch (s) {
      case 'EN_ATTENTE': return 'En attente';
      case 'EN_COURS': return 'En cours';
      case 'PRISE_EN_CHARGE': return 'Prise en charge';
      case 'TERMINEE': return 'Terminée';
      case 'REJETEE': return 'Rejetée';
      default: return s;
    }
  }
}

