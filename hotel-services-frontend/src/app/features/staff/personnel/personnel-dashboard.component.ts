import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { StaffService } from '../../../core/services/staff.service';
import { StaffDemande, StaffMember } from '../../../core/models/staff.model';
import { NotificationWebSocketService } from '../../../core/services/notification-websocket.service';
import { NotificationBellComponent } from '../../../shared/notification-bell/notification-bell.component';

@Component({
  selector: 'app-personnel-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, NotificationBellComponent],
  templateUrl: './personnel-dashboard.component.html',
  styleUrl: './personnel-dashboard.component.scss',
})
export class PersonnelDashboardComponent implements OnInit, OnDestroy {
  departmentTasks = signal<StaffDemande[]>([]);
  team = signal<StaffMember[]>([]);
  alerts = signal<StaffDemande[]>([]);
  isLoading = signal(true);
  toastMsg = signal('');
  toastError = signal(false);
  activeTab: 'tasks' | 'team' | 'alerts' = 'tasks';

  // Assignation modal
  showAssignModal = false;
  selectedTask: StaffDemande | null = null;
  selectedEmployeId: number | null = null;

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
    this.staffApi.getDepartmentTasks().subscribe({
      next: data => {
        this.departmentTasks.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Erreur chargement tâches département:', err);
        this.isLoading.set(false);
      },
    });
    this.staffApi.getDepartmentTeam().subscribe({
      next: data => this.team.set(data),
      error: (err) => console.error('Erreur chargement équipe:', err),
    });
    this.staffApi.getDepartmentAlerts().subscribe({
      next: data => this.alerts.set(data),
      error: (err) => console.error('Erreur chargement alertes:', err),
    });
  }

  openAssignModal(task: StaffDemande): void {
    this.selectedTask = task;
    this.selectedEmployeId = null;
    this.showAssignModal = true;
  }

  closeAssignModal(): void {
    this.showAssignModal = false;
    this.selectedTask = null;
  }

  confirmAssign(): void {
    if (!this.selectedTask || !this.selectedEmployeId) return;
    this.staffApi.assignTask(this.selectedTask.idDemande, this.selectedEmployeId).subscribe({
      next: () => {
        this.showToast('Tâche affectée avec succès !', false);
        this.closeAssignModal();
        this.loadAll();
      },
      error: (err) => this.showToast(err.error?.message || 'Erreur lors de l\'affectation.', true),
    });
  }

  get pendingSupervisionCount(): number {
    return this.departmentTasks().filter(t =>
      t.necessiteSupervision && (t.statut === 'EN_ATTENTE') && !t.idEmployeAssigne
    ).length;
  }

  get inProgressCount(): number {
    return this.departmentTasks().filter(t => t.statut === 'EN_COURS' || t.statut === 'PRISE_EN_CHARGE').length;
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

  get currentUser() { return this.auth.currentUser(); }

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
      default: return s;
    }
  }
}
