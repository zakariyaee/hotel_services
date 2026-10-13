import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HotelServiceService } from '../../core/services/service.service';
import { DemandeService } from '../../core/services/demande.service';
import { Service } from '../../core/models/service.model';
import { Demande, CreateDemandeDto } from '../../core/models/demande.model';
import { PrioriteDemande } from '../../core/models/enums.model';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-demande-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './demande-management.component.html',
  styleUrl: './demande-management.component.scss',
})
export class DemandeManagementComponent implements OnInit {
  services = signal<Service[]>([]);
  demandes = signal<Demande[]>([]);
  isLoading = signal<boolean>(false);
  isSubmitting = signal<boolean>(false);
  successMessage = signal<string>('');
  errorMessage = signal<string>('');

  // Formulaire de nouvelle demande
  selectedServiceId: number | null = null;
  selectedPriorite: PrioriteDemande = 'NORMALE';
  customNotes = '';
  tempsTraitementEstime: number = 30;

  readonly priorites: PrioriteDemande[] = ['BASSE', 'NORMALE', 'HAUTE', 'URGENTE'];

  constructor(
    private serviceService: HotelServiceService,
    private demandeService: DemandeService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.serviceService.getAvailable().subscribe({
      next: (services) => {
        this.services.set(services);
        if (services.length > 0 && !this.selectedServiceId) {
          this.onSelectService(services[0].idService!);
        }
      },
      error: (err) => {
        console.error('Erreur chargement services', err);
        this.errorMessage.set('Impossible de charger les services depuis le backend.');
      },
    });

    this.loadDemandes();
  }

  loadDemandes(): void {
    const user = this.authService.currentUser();
    const demandes$ = user?.role === 'CLIENT'
      ? this.demandeService.getByClient(user.idUtilisateur)
      : this.demandeService.getAll();
    demandes$.subscribe({
      next: (demandes) => {
        this.demandes.set(demandes);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Erreur chargement demandes', err);
        this.isLoading.set(false);
      },
    });
  }

  isStaff(): boolean {
    return this.authService.currentUser()?.role === 'STAFF';
  }

  updateStatus(id: number, statut: string): void {
    this.demandeService.update(id, { statut: statut as any }).subscribe({
      next: () => { this.successMessage.set('Statut de la demande mis à jour.'); this.loadDemandes(); },
      error: () => this.errorMessage.set('Impossible de modifier le statut.'),
    });
  }

  deleteDemande(id: number): void {
    if (!confirm('Supprimer cette demande ?')) return;
    this.demandeService.delete(id).subscribe({
      next: () => { this.successMessage.set('Demande supprimée.'); this.loadDemandes(); },
      error: () => this.errorMessage.set('Impossible de supprimer la demande.'),
    });
  }

  onSelectService(id: number): void {
    this.selectedServiceId = id;
    const s = this.services().find((srv) => srv.idService === id);
    if (s) {
      this.tempsTraitementEstime = s.dureeEstimee;
    }
  }

  getSelectedService(): Service | undefined {
    return this.services().find((s) => s.idService === this.selectedServiceId);
  }

  submitDemande(): void {
    if (!this.selectedServiceId) {
      this.errorMessage.set('Veuillez sélectionner un service.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const dto: CreateDemandeDto = {
      serviceId: this.selectedServiceId,
      clientId: this.authService.currentUser()?.idUtilisateur,
      priorite: this.selectedPriorite,
      tempsTraitementEstime: this.tempsTraitementEstime,
    };

    this.demandeService.create(dto).subscribe({
      next: (created) => {
        this.isSubmitting.set(false);
        this.successMessage.set(`Demande ${created.referenceDemande} enregistrée avec succès !`);
        this.customNotes = '';
        this.loadDemandes();

        setTimeout(() => this.successMessage.set(''), 6000);
      },
      error: (err) => {
        console.error('Erreur création demande', err);
        this.isSubmitting.set(false);
        this.errorMessage.set('Échec de la soumission de la demande.');
      },
    });
  }

  getPrioriteClass(priorite: string): string {
    switch (priorite) {
      case 'URGENTE': return 'badge-urgente';
      case 'HAUTE': return 'badge-haute';
      case 'NORMALE': return 'badge-normale';
      default: return 'badge-basse';
    }
  }

  getStatutClass(statut: string): string {
    switch (statut) {
      case 'TERMINEE': return 'statut-terminee';
      case 'EN_COURS': return 'statut-encours';
      case 'PRISE_EN_CHARGE': return 'statut-prise';
      case 'REJETEE':
      case 'ANNULEE': return 'statut-annulee';
      default: return 'statut-attente';
    }
  }
}
