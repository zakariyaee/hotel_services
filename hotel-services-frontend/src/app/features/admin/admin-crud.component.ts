import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HotelServiceService } from '../../core/services/service.service';
import { DepartementService } from '../../core/services/departement.service';
import { ChambreService } from '../../core/services/chambre.service';
import { AdminReservationService } from '../../core/services/admin-reservation.service';
import { AdminUtilisateurService } from '../../core/services/admin-utilisateur.service';
import { Service } from '../../core/models/service.model';
import { Departement } from '../../core/models/departement.model';
import { Chambre } from '../../core/models/chambre.model';
import { AdminReservation, CreateReservationDto } from '../../core/models/admin-reservation.model';
import { AdminUtilisateur, CreateAdminUtilisateurDto } from '../../core/models/admin-utilisateur.model';
import { Role } from '../../core/models/enums.model';

@Component({
  selector: 'app-admin-crud',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-crud.component.html',
  styleUrl: './admin-crud.component.scss',
})
export class AdminCrudComponent implements OnInit {
  // Navigation tabs in Admin
  activeTab: 'reservations' | 'services' | 'departements' | 'utilisateurs' = 'reservations';

  // Signals for data
  readonly reservations = signal<AdminReservation[]>([]);
  readonly services = signal<Service[]>([]);
  readonly departements = signal<Departement[]>([]);
  readonly chambres = signal<Chambre[]>([]);
  readonly utilisateurs = signal<AdminUtilisateur[]>([]);
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

  // Utilisateur & Affectation State
  showUserModal = false;
  editingUserId: number | null = null;
  userRoleFilter: string = 'ALL';
  userForm: CreateAdminUtilisateurDto = {
    nom: '',
    prenom: '',
    email: '',
    motDePasse: '',
    tel: '',
    role: 'PERSONNEL' as Role,
    idDepartement: 1,
    poste: 'Superviseur Restauration',
  };

  // Assignation directe depuis l'onglet Départements
  showAssignDeptModal = false;
  selectedDeptForAssignment: Departement | null = null;
  selectedPersonnelIdToAssign: number | null = null;
  assignedPosteInput: string = '';
  assignModalRoleFilter: 'ALL' | 'PERSONNEL' | 'EMPLOYE' | 'RECEPTIONNISTE' | 'UNASSIGNED' = 'ALL';
  assignModalSearchQuery: string = '';

  constructor(
    private serviceApi: HotelServiceService,
    private departementApi: DepartementService,
    private chambreApi: ChambreService,
    private adminReservationApi: AdminReservationService,
    private utilisateurApi: AdminUtilisateurService
  ) {}

  ngOnInit(): void {
    this.reloadAll();
  }

  setTab(tab: 'reservations' | 'services' | 'departements' | 'utilisateurs'): void {
    this.activeTab = tab;
    this.message.set('');
  }

  reloadAll(): void {
    this.reloadReservations();
    this.reloadServices();
    this.reloadDepartements();
    this.reloadChambres();
    this.reloadUtilisateurs();
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

  // --- GESTION DES UTILISATEURS & AFFECTATION AU PERSONNEL ---
  reloadUtilisateurs(): void {
    this.utilisateurApi.getAll().subscribe({
      next: data => this.utilisateurs.set(data),
      error: err => console.warn('Could not load users', err),
    });
  }

  get filteredUtilisateurs(): AdminUtilisateur[] {
    const list = this.utilisateurs();
    if (this.userRoleFilter === 'ALL') return list;
    return list.filter(u => u.role === this.userRoleFilter);
  }

  openCreateUserModal(): void {
    this.editingUserId = null;
    const defaultDept = this.departements()[0]?.idDepartement ?? 1;
    this.userForm = {
      nom: '',
      prenom: '',
      email: '',
      motDePasse: 'staff123',
      tel: '',
      role: 'PERSONNEL' as Role,
      idDepartement: defaultDept,
      poste: 'Superviseur Restauration',
    };
    this.showUserModal = true;
  }

  openEditUserModal(user: AdminUtilisateur): void {
    this.editingUserId = user.idUtilisateur;
    this.userForm = {
      nom: user.nom,
      prenom: user.prenom,
      email: user.email,
      motDePasse: '',
      tel: user.tel ?? '',
      role: user.role,
      idDepartement: user.idDepartement ?? (this.departements()[0]?.idDepartement ?? 1),
      poste: user.poste ?? '',
    };
    this.showUserModal = true;
  }

  closeUserModal(): void {
    this.showUserModal = false;
    this.editingUserId = null;
  }

  onRoleChange(newRole: string): void {
    this.userForm.role = newRole as Role;
    if (newRole === 'PERSONNEL' && !this.userForm.poste) {
      this.userForm.poste = 'Superviseur';
    } else if (newRole === 'EMPLOYE' && !this.userForm.poste) {
      this.userForm.poste = 'Agent de service';
    } else if (newRole === 'RECEPTIONNISTE' && !this.userForm.poste) {
      this.userForm.poste = 'Réceptionniste';
    }
  }

  saveUser(): void {
    if (!this.userForm.nom.trim() || !this.userForm.prenom.trim() || !this.userForm.email.trim()) {
      this.message.set('Veuillez renseigner les champs obligatoires (nom, prénom, email).');
      return;
    }

    this.isLoading.set(true);
    const op = this.editingUserId === null
      ? this.utilisateurApi.create(this.userForm)
      : this.utilisateurApi.update(this.editingUserId, this.userForm);

    op.subscribe({
      next: () => {
        this.isLoading.set(false);
        this.message.set(this.editingUserId === null ? 'Utilisateur créé et affecté avec succès !' : 'Utilisateur mis à jour avec succès !');
        this.closeUserModal();
        this.reloadUtilisateurs();
      },
      error: (err) => {
        this.isLoading.set(false);
        this.message.set(err.error?.message || 'Erreur lors de l\'enregistrement de l\'utilisateur.');
      },
    });
  }

  deleteUser(id: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) return;
    this.utilisateurApi.delete(id).subscribe({
      next: () => {
        this.message.set('Utilisateur supprimé avec succès.');
        this.reloadUtilisateurs();
      },
      error: (err) => this.message.set(err.error?.message || 'Impossible de supprimer cet utilisateur.'),
    });
  }

  // --- ACTIONS D'ASSIGNATION DEPUIS L'ONGLET DÉPARTEMENTS ---
  getPersonnelForDepartement(deptId: number | undefined): AdminUtilisateur[] {
    if (!deptId) return [];
    return this.utilisateurs().filter(u => u.idDepartement === deptId);
  }

  getSuperviseursForDept(deptId: number | undefined): AdminUtilisateur[] {
    if (!deptId) return [];
    return this.utilisateurs().filter(u => u.idDepartement === deptId && u.role === 'PERSONNEL');
  }

  getEmployesForDept(deptId: number | undefined): AdminUtilisateur[] {
    if (!deptId) return [];
    return this.utilisateurs().filter(u => u.idDepartement === deptId && (u.role === 'EMPLOYE' || u.role === 'STAFF'));
  }

  get availablePersonnelForAssignment(): AdminUtilisateur[] {
    // Tous les membres du staff (PERSONNEL, EMPLOYE, RECEPTIONNISTE) - exclut CLIENT et ADMIN
    return this.utilisateurs().filter(u =>
      u.role === 'PERSONNEL' || u.role === 'EMPLOYE' || u.role === 'RECEPTIONNISTE' || u.role === 'STAFF'
    );
  }

  get filteredPersonnelForAssignment(): AdminUtilisateur[] {
    let list = this.availablePersonnelForAssignment;

    // Filtre par rôle
    if (this.assignModalRoleFilter === 'PERSONNEL') {
      list = list.filter(u => u.role === 'PERSONNEL');
    } else if (this.assignModalRoleFilter === 'EMPLOYE') {
      list = list.filter(u => u.role === 'EMPLOYE' || u.role === 'STAFF');
    } else if (this.assignModalRoleFilter === 'RECEPTIONNISTE') {
      list = list.filter(u => u.role === 'RECEPTIONNISTE');
    } else if (this.assignModalRoleFilter === 'UNASSIGNED') {
      list = list.filter(u => !u.idDepartement);
    }

    // Filtre par recherche textuelle (nom, prénom, email, poste)
    if (this.assignModalSearchQuery && this.assignModalSearchQuery.trim()) {
      const q = this.assignModalSearchQuery.toLowerCase().trim();
      list = list.filter(u =>
        u.nom.toLowerCase().includes(q) ||
        u.prenom.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        (u.poste && u.poste.toLowerCase().includes(q))
      );
    }

    return list;
  }

  openAssignPersonnelModal(dept: Departement): void {
    this.selectedDeptForAssignment = dept;
    this.selectedPersonnelIdToAssign = null;
    this.assignedPosteInput = '';
    this.assignModalRoleFilter = 'ALL';
    this.assignModalSearchQuery = '';
    this.showAssignDeptModal = true;
  }

  closeAssignPersonnelModal(): void {
    this.showAssignDeptModal = false;
    this.selectedDeptForAssignment = null;
  }

  onPersonnelSelectedForAssignment(userIdStr: string): void {
    const id = Number(userIdStr);
    this.selectedPersonnelIdToAssign = id;
    const u = this.utilisateurs().find(x => x.idUtilisateur === id);
    if (u) {
      this.assignedPosteInput = u.poste || (u.role === 'PERSONNEL' ? 'Superviseur' : 'Agent');
    }
  }

  confirmAssignPersonnelToDept(): void {
    if (!this.selectedDeptForAssignment?.idDepartement || !this.selectedPersonnelIdToAssign) {
      this.message.set('Veuillez sélectionner un membre du personnel.');
      return;
    }

    this.isLoading.set(true);
    this.utilisateurApi.assignDepartement(
      this.selectedPersonnelIdToAssign,
      this.selectedDeptForAssignment.idDepartement,
      this.assignedPosteInput
    ).subscribe({
      next: (updatedUser) => {
        this.isLoading.set(false);
        this.message.set(`${updatedUser.prenom} ${updatedUser.nom} a été assigné(e) avec succès au département « ${this.selectedDeptForAssignment?.nom} » !`);
        this.reloadUtilisateurs();
        this.selectedPersonnelIdToAssign = null;
        this.assignedPosteInput = '';
      },
      error: (err) => {
        this.isLoading.set(false);
        this.message.set(err.error?.message || 'Erreur lors de l\'assignation du personnel.');
      },
    });
  }

  unassignPersonnelFromDept(userId: number, event?: Event): void {
    if (event) event.stopPropagation();
    if (!confirm('Désassigner ce membre de ce département ?')) return;

    this.isLoading.set(true);
    this.utilisateurApi.unassignDepartement(userId).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.message.set('Personnel retiré du département avec succès.');
        this.reloadUtilisateurs();
      },
      error: (err) => {
        this.isLoading.set(false);
        this.message.set(err.error?.message || 'Impossible de retirer le personnel.');
      },
    });
  }
}
