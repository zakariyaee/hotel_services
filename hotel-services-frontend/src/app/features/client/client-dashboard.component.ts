import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { HotelServiceService } from '../../core/services/service.service';
import { DemandeService } from '../../core/services/demande.service';
import { Service } from '../../core/models/service.model';
import { Demande, CreateDemandeDto, HistoriqueDemande } from '../../core/models/demande.model';
import { NotificationWebSocketService } from '../../core/services/notification-websocket.service';
import { NotificationBellComponent } from '../../shared/notification-bell/notification-bell.component';

export interface ServiceCardDef {
  id: string;
  title: string;
  subtitle: string;
  estimate: string;
  defaultMinutes: number;
  icon: 'utensils' | 'sparkles' | 'laundry' | 'wrench' | 'bell' | 'clock';
  iconClass: string;
  serviceCategory: string;
  formTitle: string;
  formDesc: string;
  needs: string[];
  placeholder: string;
  backendId: number;
}

export interface ActiveDemandeDisplay {
  idDemande?: number;
  reference: string;
  title: string;
  status: string;
  statusLabel: string;
  etaText: string;
  dateCreation?: string;
  notes?: string;
  priorite?: string;
}

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, NotificationBellComponent],
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.scss',
})
export class ClientDashboardComponent implements OnInit, OnDestroy {
  // Navigation view: 'hub' (Screen 1) or 'request-form' (Screen 2)
  currentView: 'hub' | 'request-form' = 'hub';

  // Backend state
  services = signal<Service[]>([]);
  demandes = signal<Demande[]>([]);
  isLoading = signal<boolean>(false);
  isSubmitting = signal<boolean>(false);
  toastMessage = signal<string>('');

  // Demandes status filter: 'ALL' | 'EN_COURS' | 'EN_ATTENTE' | 'TERMINEE'
  activeStatusFilter: 'ALL' | 'EN_COURS' | 'EN_ATTENTE' | 'TERMINEE' = 'ALL';

  // Dropdown & Modal states
  showUserMenu = false;
  showNotifications = false;
  showDemandDetailModal = false;
  selectedModalDemande: ActiveDemandeDisplay | null = null;
  demandeHistory = signal<HistoriqueDemande[]>([]);

  // Les cartes sont construites depuis le catalogue administré en base.
  serviceCards: ServiceCardDef[] = [];

  // Screen 2 Form State
  selectedServiceCard: ServiceCardDef | null = null;
  selectedTiming: 'asap' | '1h' | 'custom' = 'asap';
  customSelectedTime = '08:30';
  selectedNeeds: string[] = [];
  customNotes = '';

  constructor(
    public auth: AuthService,
    private router: Router,
    private serviceService: HotelServiceService,
    private demandeService: DemandeService,
    public wsNotif: NotificationWebSocketService
  ) {}

  ngOnInit(): void {
    this.loadBackendData();
    const token = this.auth.currentUser()?.token || this.auth.getToken();
    if (token) this.wsNotif.connect(token);
  }

  ngOnDestroy(): void {
    this.wsNotif.disconnect();
  }

  get currentUser() {
    return this.auth.currentUser;
  }

  get userDisplayName(): string {
    const email = this.currentUser()?.email;
    if (email) {
      const name = email.split('@')[0];
      return name.charAt(0).toUpperCase() + name.slice(1);
    }
    return 'Zakaria';
  }

  get clientFirstName(): string {
    const email = this.currentUser()?.email;
    if (email) {
      const part = email.split('@')[0].split('.')[0];
      return part.charAt(0).toUpperCase() + part.slice(1);
    }
    return 'Camille';
  }

  get roomNumber(): string {
    return this.auth.getRoomNumber();
  }

  get roomType(): string {
    return this.auth.getRoomType();
  }

  get stayBadgeText(): string {
    return 'SÉJOUR EN COURS · 5 ÉTOILES';
  }

  get userInitials(): string {
    const email = this.currentUser()?.email;
    if (email) {
      const parts = email.split('@')[0].split('.');
      if (parts.length >= 2) {
        return (parts[0][0] + parts[1][0]).toUpperCase();
      }
      return email.slice(0, 2).toUpperCase();
    }
    return 'CL';
  }

  get currentEstimatedTimeText(): string {
    if (!this.selectedServiceCard) return '15-25 min';
    if (this.selectedTiming === 'asap') {
      return this.selectedServiceCard.estimate;
    }
    if (this.selectedTiming === '1h') {
      return 'Dans 1 heure';
    }
    return `À ${this.customSelectedTime || 'l\'heure choisie'}`;
  }

  // Demandes filtering & stats
  get filteredDemandes(): Demande[] {
    const list = this.demandes();
    if (this.activeStatusFilter === 'EN_COURS') {
      return list.filter((d) => d.statut === 'EN_COURS' || d.statut === 'PRISE_EN_CHARGE');
    }
    if (this.activeStatusFilter === 'EN_ATTENTE') {
      return list.filter((d) => d.statut === 'EN_ATTENTE');
    }
    if (this.activeStatusFilter === 'TERMINEE') {
      return list.filter((d) => d.statut === 'TERMINEE');
    }
    return list;
  }

  get countEnCours(): number {
    return this.demandes().filter((d) => d.statut === 'EN_COURS' || d.statut === 'PRISE_EN_CHARGE').length;
  }

  get countEnAttente(): number {
    return this.demandes().filter((d) => d.statut === 'EN_ATTENTE').length;
  }

  get countTerminees(): number {
    return this.demandes().filter((d) => d.statut === 'TERMINEE').length;
  }

  setStatusFilter(filter: 'ALL' | 'EN_COURS' | 'EN_ATTENTE' | 'TERMINEE'): void {
    this.activeStatusFilter = filter;
  }

  loadBackendData(): void {
    this.isLoading.set(true);

    // 1. Le client ne voit que les services actifs du catalogue admin.
    this.serviceService.getAvailable().subscribe({
      next: (svcs) => {
        this.services.set(svcs);
        this.mapServicesToCards(svcs);
      },
      error: (err) => console.warn('Could not load services from backend', err),
    });

    // 2. Fetch all demandes for this client
    this.refreshDemandes();
  }

  refreshDemandes(): void {
    const user = this.currentUser();
    if (!user) return;`r`
       const clientId = user.idUtilisateur;

    this.demandeService.getByClient(clientId).subscribe({
      next: (demandesList) => {
        // Sort descending by id or date so latest is first
        const sorted = [...demandesList].sort((a, b) => (b.idDemande || 0) - (a.idDemande || 0));
        this.demandes.set(sorted);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.warn('Could not load client demandes', err);
        this.isLoading.set(false);
      },
    });
  }

  private mapServicesToCards(svcs: Service[]): void {
    this.serviceCards = svcs
      .filter(service => service.disponibilite && service.idService)
      .map((service, index) => this.toServiceCard(service, index));
  }

  private toServiceCard(service: Service, index: number): ServiceCardDef {
    const title = service.nom.trim();
    const minutes = service.dureeEstimee > 0 ? service.dureeEstimee : 30;
    const icon = this.getServiceIcon(title, service.categorie);
    const category = service.categorie?.trim() || service.nomDepartement?.trim() || 'SERVICE HÔTELIER';

    return {
      id: String(service.idService),
      title,
      subtitle: service.description?.trim() || `Prestation ${category.toLowerCase()}`,
      estimate: `${Math.max(5, minutes - 5)}-${minutes + 5} min`,
      defaultMinutes: minutes,
      icon,
      iconClass: `icon-service-${index % 6}`,
      serviceCategory: category.toUpperCase(),
      formTitle: `Demander : ${title}`,
      formDesc: service.description?.trim() || `Notre équipe prend en charge votre demande de ${title.toLowerCase()}.`,
      needs: ['Demande standard', 'Préférence particulière'],
      placeholder: `Précisez votre besoin concernant « ${title} »...`,
      backendId: service.idService!,
    };
  }

  private getServiceIcon(name: string, category?: string): ServiceCardDef['icon'] {
    const value = `${name} ${category ?? ''}`.toLowerCase();
    if (value.includes('ménage') || value.includes('netto') || value.includes('housekeeping')) return 'sparkles';
    if (value.includes('blanch') || value.includes('linge')) return 'laundry';
    if (value.includes('maintenance') || value.includes('technique') || value.includes('répar')) return 'wrench';
    if (value.includes('concier') || value.includes('réserv')) return 'bell';
    if (value.includes('réveil') || value.includes('appel')) return 'clock';
    return 'utensils';
  }

  // Navigation handlers
  openServiceRequest(card: ServiceCardDef): void {
    this.selectedServiceCard = card;
    this.selectedTiming = 'asap';
    this.customNotes = '';

    this.selectedNeeds = card.needs.length > 0 ? [card.needs[0]] : [];

    this.currentView = 'request-form';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  backToHub(): void {
    this.currentView = 'hub';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // Form interactions
  setTiming(timing: 'asap' | '1h' | 'custom'): void {
    this.selectedTiming = timing;
  }

  isNeedSelected(need: string): boolean {
    return this.selectedNeeds.includes(need);
  }

  toggleNeed(need: string): void {
    if (this.isNeedSelected(need)) {
      this.selectedNeeds = this.selectedNeeds.filter((n) => n !== need);
    } else {
      this.selectedNeeds.push(need);
    }
  }

  // Submit Demande and store into MySQL database
  submitDemand(): void {
    if (!this.selectedServiceCard) return;

    this.isSubmitting.set(true);

    const refNum = Math.floor(1000 + Math.random() * 9000);
    const reference = `DEM-${new Date().getFullYear()}-${refNum}`;
    const user = this.currentUser();`r`
        if (!user) { this.isSubmitting.set(false); return; }`r`
          const clientId = user.idUtilisateur;
    const serviceId = this.selectedServiceCard.backendId || 1;

    let delayMinutes = this.selectedServiceCard.defaultMinutes;
    if (this.selectedTiming === '1h') delayMinutes = 60;

    const dto: CreateDemandeDto = {
      serviceId: serviceId,
      clientId: clientId,
      priorite: this.selectedTiming === 'asap' ? 'HAUTE' : 'NORMALE',
      referenceDemande: reference,
      tempsTraitementEstime: delayMinutes,
      commentairesClient: this.customNotes?.trim() || undefined,
    };

    this.demandeService.create(dto).subscribe({
      next: (created) => {
        this.isSubmitting.set(false);
        this.toastMessage.set(`Votre demande ${created.referenceDemande || reference} a bien été enregistrée dans la base de données !`);
        this.currentView = 'hub';
        this.activeStatusFilter = 'ALL';
        this.refreshDemandes();
        setTimeout(() => this.toastMessage.set(''), 6000);
      },
      error: (err) => {
        console.error('Erreur lors de la création de la demande', err);
        this.isSubmitting.set(false);
        this.toastMessage.set(`Échec de l'enregistrement: ${err.error?.message || err.message || 'Erreur serveur'}`);
        setTimeout(() => this.toastMessage.set(''), 6000);
      },
    });
  }

  // Labels & status helpers
  getStatusTag(statut?: string): string {
    switch (statut) {
      case 'EN_COURS':
        return 'Demande en cours';
      case 'PRISE_EN_CHARGE':
        return 'Demande prise en charge';
      case 'EN_ATTENTE':
        return 'Demande en attente';
      case 'TERMINEE':
        return 'Demande terminée';
      case 'ANNULEE':
        return 'Demande annulée';
      default:
        return 'Demande enregistrée';
    }
  }

  getStatutLabel(statut?: string): string {
    switch (statut) {
      case 'EN_COURS':
        return 'En route vers votre chambre';
      case 'PRISE_EN_CHARGE':
        return 'Prise en charge par notre équipe';
      case 'TERMINEE':
        return 'Demande traitée et terminée';
      case 'ANNULEE':
        return 'Demande annulée';
      case 'REJETEE':
        return 'Non réalisable';
      default:
        return 'En attente de prise en charge';
    }
  }

  getStatutDotClass(statut?: string): string {
    switch (statut) {
      case 'EN_COURS':
        return 'dot-encours';
      case 'PRISE_EN_CHARGE':
        return 'dot-prise';
      case 'TERMINEE':
        return 'dot-terminee';
      case 'ANNULEE':
      case 'REJETEE':
        return 'dot-annulee';
      default:
        return 'dot-attente';
    }
  }

  getServiceBadgeColor(serviceName?: string): string {
    const s = (serviceName || '').toLowerCase();
    if (s.includes('room') || s.includes('repas') || s.includes('petit')) return 'badge-food';
    if (s.includes('nage') || s.includes('ménage') || s.includes('linge')) return 'badge-housekeeping';
    if (s.includes('blanchisserie')) return 'badge-laundry';
    if (s.includes('technique') || s.includes('maintenance')) return 'badge-tech';
    if (s.includes('concierge') || s.includes('navette')) return 'badge-concierge';
    return 'badge-default';
  }

  // Modals & Menu
  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
    this.showNotifications = false;
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    this.showUserMenu = false;
  }

  // Cancellation state
  showCancelConfirm = false;
  cancelReason = '';
  isCancelling = signal(false);

  openDemandeDetail(d: Demande): void {
    this.selectedModalDemande = {
      idDemande: d.idDemande,
      reference: d.referenceDemande || `#${d.idDemande}`,
      title: d.service?.nom || 'Demande de service',
      status: d.statut,
      statusLabel: this.getStatutLabel(d.statut),
      etaText: `~ ${d.tempsTraitementEstime || 20} min`,
      dateCreation: d.dateCreation,
      priorite: d.priorite,
    };
    this.showCancelConfirm = false;
    this.cancelReason = '';
    this.demandeHistory.set([]);
    if (d.idDemande) {
      this.demandeService.getHistory(d.idDemande).subscribe({
        next: history => this.demandeHistory.set(history),
        error: () => {},
      });
    }
    this.showDemandDetailModal = true;
  }

  closeDemandModal(): void {
    this.showDemandDetailModal = false;
    this.selectedModalDemande = null;
    this.demandeHistory.set([]);
    this.showCancelConfirm = false;
    this.cancelReason = '';
  }

  initiateCancel(): void {
    this.showCancelConfirm = true;
    this.cancelReason = '';
  }

  abortCancel(): void {
    this.showCancelConfirm = false;
    this.cancelReason = '';
  }

  confirmCancel(): void {
    if (!this.selectedModalDemande?.idDemande) return;

    this.isCancelling.set(true);
    const id = this.selectedModalDemande.idDemande;
    const motif = this.cancelReason?.trim() || 'Annulée à la demande du client';

    this.demandeService.update(id, {
      statut: 'ANNULEE' as any,
      motifAnnulation: motif,
    }).subscribe({
      next: () => {
        this.isCancelling.set(false);
        this.toastMessage.set('Votre demande a bien été annulée.');
        this.closeDemandModal();
        this.refreshDemandes();
        setTimeout(() => this.toastMessage.set(''), 5000);
      },
      error: (err) => {
        this.isCancelling.set(false);
        this.toastMessage.set(err.error?.message || "Échec de l'annulation de la demande.");
        setTimeout(() => this.toastMessage.set(''), 5000);
      }
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}


