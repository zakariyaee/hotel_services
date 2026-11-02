import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { StaffService } from '../../../core/services/staff.service';
import { HotelServiceService } from '../../../core/services/service.service';
import { StaffDemande, ReceptionOverview, RoomGuestOption, ReceptionCreateRequest } from '../../../core/models/staff.model';
import { Service } from '../../../core/models/service.model';
import { StatutDemande, PrioriteDemande } from '../../../core/models/enums.model';

@Component({
  selector: 'app-reception-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reception-dashboard.component.html',
  styleUrl: './reception-dashboard.component.scss',
})
export class ReceptionDashboardComponent implements OnInit {
  overview = signal<ReceptionOverview | null>(null);
  demandes = signal<StaffDemande[]>([]);
  roomGuests = signal<RoomGuestOption[]>([]);
  services = signal<Service[]>([]);
  isLoading = signal(true);
  toastMsg = signal('');
  toastError = signal(false);

  activeTab: 'overview' | 'demandes' | 'create' = 'overview';

  // Filters
  searchQuery = '';
  filterStatut: StatutDemande | '' = '';

  // Create demand form
  createForm: ReceptionCreateRequest = {
    idClient: 0,
    idService: 0,
    priorite: 'NORMALE',
    commentairesClient: '',
  };
  isCreating = signal(false);

  constructor(
    public auth: AuthService,
    private staffApi: StaffService,
    private serviceApi: HotelServiceService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading.set(true);
    this.staffApi.getReceptionOverview().subscribe({
      next: data => { this.overview.set(data); this.isLoading.set(false); },
      error: () => this.isLoading.set(false),
    });
    this.loadDemandes();
    this.staffApi.getRoomGuests().subscribe({
      next: data => this.roomGuests.set(data),
    });
    this.serviceApi.getAll().subscribe({
      next: data => this.services.set(data),
    });
  }

  loadDemandes(): void {
    const statut = this.filterStatut || undefined;
    this.staffApi.getReceptionDemandes(this.searchQuery || undefined, undefined, statut).subscribe({
      next: data => this.demandes.set(data),
    });
  }

  createDemand(): void {
    if (!this.createForm.idClient || !this.createForm.idService) {
      this.showToast('Veuillez sélectionner un client et un service.', true);
      return;
    }
    this.isCreating.set(true);
    this.staffApi.receptionCreateDemand(this.createForm).subscribe({
      next: () => {
        this.isCreating.set(false);
        this.showToast('Demande créée avec succès !', false);
        this.createForm = { idClient: 0, idService: 0, priorite: 'NORMALE', commentairesClient: '' };
        this.loadAll();
        this.activeTab = 'demandes';
      },
      error: (err) => {
        this.isCreating.set(false);
        this.showToast(err.error?.message || 'Erreur lors de la création.', true);
      },
    });
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

  get filteredDemandes(): StaffDemande[] {
    return this.demandes();
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

