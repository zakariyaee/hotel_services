import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HotelServiceService } from '../../core/services/service.service';
import { DepartementService } from '../../core/services/departement.service';
import { ChambreService } from '../../core/services/chambre.service';
import { AdminReservationService } from '../../core/services/admin-reservation.service';
import { Service } from '../../core/models/service.model';
import { Departement } from '../../core/models/departement.model';
import { Chambre } from '../../core/models/chambre.model';
import { AdminReservation, CreateReservationDto } from '../../core/models/admin-reservation.model';

@Component({
  selector: 'app-admin-crud',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-crud.component.html',
  styleUrl: './admin-crud.component.scss',
})
export class AdminCrudComponent implements OnInit {
  // Navigation tabs in Admin
  activeTab: 'reservations' | 'services' | 'departements' = 'reservations';

  // Signals for data
  readonly reservations = signal<AdminReservation[]>([]);
  readonly services = signal<Service[]>([]);
  readonly departements = signal<Departement[]>([]);
  readonly chambres = signal<Chambre[]>([]);
  readonly message = signal('');
  readonly isLoading = signal(false);

  // Reservation State & Modals
  showCreateReservationModal = false;
  showPassModal = false;
  selectedReservation: AdminReservation | null = null;
  copiedLink = false;

  reservationForm: CreateReservationDto = {
    clientNom: '',
    clientPrenom: '',
    clientEmail: '',
    clientTel: '',
    idChambre: 1,
    dateDebut: new Date().toISOString().split('T')[0],
    dateFin: new Date(Date.now() + 7 * 86400000).toISOString().split('T')[0],
    typeReservation: 'Séjour Prestige & Confort',
  };

  // Service State
  serviceId: number | null = null;
  serviceForm = {
    nom: '',
    description: '',
    categorie: 'Room Service',
    dureeEstimee: 30,
    prixIndicatif: 0,
    disponibilite: true,
    necessiteSupervision: false,
    imageUrl: '',
    idDepartement: 1,
  };

  // Departement State
  departementId: number | null = null;
  departementForm = { nom: '', description: '', horairesOuverture: '' };

  constructor(
    private serviceApi: HotelServiceService,
    private departementApi: DepartementService,
    private chambreApi: ChambreService,
    private adminReservationApi: AdminReservationService
  ) {}

  ngOnInit(): void {
    this.reloadAll();
  }

  setTab(tab: 'reservations' | 'services' | 'departements'): void {
    this.activeTab = tab;
    this.message.set('');
  }

  reloadAll(): void {
    this.reloadReservations();
    this.reloadServices();
    this.reloadDepartements();
    this.reloadChambres();
  }

  reloadReservations(): void {
    this.adminReservationApi.getAll().subscribe({
      next: data => this.reservations.set(data),
      error: err => console.warn('Could not load reservations', err),
    });
  }

  reloadServices(): void {
    this.serviceApi.getAll().subscribe({
      next: data => this.services.set(data),
      error: err => console.warn('Could not load services', err),
    });
  }

  reloadDepartements(): void {
    this.departementApi.getAll().subscribe({
      next: data => this.departements.set(data),
      error: err => console.warn('Could not load departements', err),
    });
  }

  reloadChambres(): void {
    this.chambreApi.getAll().subscribe({
      next: data => {
        this.chambres.set(data);
        if (data.length > 0 && !this.reservationForm.idChambre) {
          this.reservationForm.idChambre = data[0].idChambre ?? 1;
        }
      },
      error: err => console.warn('Could not load chambres', err),
    });
  }

  // --- GESTION DES RÉSERVATIONS & PASS QR CODE ---
  openCreateReservationModal(): void {
    this.reservationForm = {
      clientNom: '',
      clientPrenom: '',
      clientEmail: '',
      clientTel: '',
      idChambre: this.chambres().length > 0 ? (this.chambres()[0].idChambre ?? 1) : 1,
      dateDebut: new Date().toISOString().split('T')[0],
      dateFin: new Date(Date.now() + 5 * 86400000).toISOString().split('T')[0],
      typeReservation: 'Séjour Prestige',
    };
    this.showCreateReservationModal = true;
  }

  closeCreateReservationModal(): void {
    this.showCreateReservationModal = false;
  }

  saveReservation(): void {
    if (!this.reservationForm.clientNom || !this.reservationForm.clientEmail) {
      this.message.set('Veuillez renseigner le nom et l’email du client.');
      return;
    }

    this.isLoading.set(true);
    this.adminReservationApi.create(this.reservationForm).subscribe({
      next: created => {
        this.isLoading.set(false);
        this.closeCreateReservationModal();
        this.reloadReservations();
        this.openPassModal(created);
        this.message.set(`Réservation créée avec succès ! Pass QR Code généré et email envoyé à ${created.clientEmail}.`);
      },
      error: err => {
        this.isLoading.set(false);
        this.message.set(`Erreur création réservation : ${err.error?.message || err.message}`);
      },
    });
  }

  openPassModal(res: AdminReservation): void {
    this.selectedReservation = res;
    this.showPassModal = true;
    this.copiedLink = false;
  }

  closePassModal(): void {
    this.showPassModal = false;
    this.selectedReservation = null;
  }

  copyLoginUrl(): void {
    if (this.selectedReservation?.loginUrl) {
      navigator.clipboard.writeText(this.selectedReservation.loginUrl);
      this.copiedLink = true;
      setTimeout(() => (this.copiedLink = false), 3000);
    }
  }

  printPass(): void {
    window.print();
  }

  // --- GESTION DES SERVICES ---
  editService(value: Service): void {
    this.serviceId = value.idService ?? null;
    this.serviceForm = {
      nom: value.nom,
      description: value.description ?? '',
      categorie: value.categorie ?? 'Room Service',
      dureeEstimee: value.dureeEstimee,
      prixIndicatif: value.prixIndicatif ?? 0,
      disponibilite: value.disponibilite,
      necessiteSupervision: value.necessiteSupervision ?? false,
      imageUrl: value.imageUrl ?? '',
      idDepartement: value.departement?.idDepartement ?? 1,
    };
  }

  saveService(): void {
    const request =
      this.serviceId === null
        ? this.serviceApi.create(this.serviceForm)
        : this.serviceApi.update(this.serviceId, this.serviceForm);

    request.subscribe({
      next: () => {
        this.message.set('Service enregistré avec succès.');
        this.cancelService();
        this.reloadServices();
      },
      error: () => this.message.set('Impossible d’enregistrer le service.'),
    });
  }

  toggleServiceStatus(id: number): void {
    this.serviceApi.delete(id); // ou toggle
  }

  deleteService(id: number): void {
    if (!confirm('Supprimer ce service ?')) return;
    this.serviceApi.delete(id).subscribe({
      next: () => {
        this.message.set('Service supprimé.');
        this.reloadServices();
      },
    });
  }

  cancelService(): void {
    this.serviceId = null;
    this.serviceForm = {
      nom: '',
      description: '',
      categorie: 'Room Service',
      dureeEstimee: 30,
      prixIndicatif: 0,
      disponibilite: true,
      necessiteSupervision: false,
      imageUrl: '',
      idDepartement: 1,
    };
  }

  // --- GESTION DES DÉPARTEMENTS ---
  editDepartement(value: Departement): void {
    this.departementId = value.idDepartement ?? null;
    this.departementForm = {
      nom: value.nom,
      description: value.description ?? '',
      horairesOuverture: value.horairesOuverture ?? '',
    };
  }

  saveDepartement(): void {
    const request =
      this.departementId === null
        ? this.departementApi.create(this.departementForm)
        : this.departementApi.update(this.departementId, this.departementForm);

    request.subscribe({
      next: () => {
        this.message.set('Département enregistré avec succès.');
        this.cancelDepartement();
        this.reloadDepartements();
      },
      error: () => this.message.set('Impossible d’enregistrer le département.'),
    });
  }

  deleteDepartement(id: number): void {
    if (!confirm('Supprimer ce département ?')) return;
    this.departementApi.delete(id).subscribe({
      next: () => {
        this.message.set('Département supprimé.');
        this.reloadDepartements();
      },
    });
  }

  cancelDepartement(): void {
    this.departementId = null;
    this.departementForm = { nom: '', description: '', horairesOuverture: '' };
  }
}
