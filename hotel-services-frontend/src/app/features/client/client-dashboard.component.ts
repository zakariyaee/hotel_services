import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { HotelServiceService } from '../../core/services/service.service';
import { DemandeService } from '../../core/services/demande.service';
import { Service } from '../../core/models/service.model';
import { Demande, CreateDemandeDto } from '../../core/models/demande.model';

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
  backendId?: number;
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
}

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.scss',
})
export class ClientDashboardComponent implements OnInit {
  // Navigation view: 'hub' (Screen 1) or 'request-form' (Screen 2)
  currentView: 'hub' | 'request-form' = 'hub';

  // Backend state
  services = signal<Service[]>([]);
  demandes = signal<Demande[]>([]);
  isLoading = signal<boolean>(false);
  isSubmitting = signal<boolean>(false);
  toastMessage = signal<string>('');

  // Dropdown states
  showUserMenu = false;
  showNotifications = false;
  showDemandDetailModal = false;
  selectedModalDemande: ActiveDemandeDisplay | null = null;

  // Active demand displayed in Screen 1 footer card
  activeDemandeDisplay: ActiveDemandeDisplay | null = {
    reference: '#2041',
    title: 'Serviettes supplémentaires',
    status: 'EN_COURS',
    statusLabel: 'En route vers votre chambre',
    etaText: '~ 8 min',
  };

  // 6 Services Cards according to Maison Élan design
  serviceCards: ServiceCardDef[] = [
    {
      id: 'room-service',
      title: 'Room service',
      subtitle: 'Carte disponible 24h/24',
      estimate: '25-35 min',
      defaultMinutes: 30,
      icon: 'utensils',
      iconClass: 'icon-room',
      serviceCategory: 'SERVICE EN CHAMBRE',
      formTitle: 'Commander le room service',
      formDesc: 'Savourez nos plateaux gourmets préparés par le chef et servis en chambre.',
      needs: ['Menu Gourmet complet', 'Plateau dégustation', 'Boissons fraîches & Vins', 'Couverts supplémentaires'],
      placeholder: 'Ex. Cuisson à point, sans gluten, ajouter des serviettes...',
    },
    {
      id: 'menage',
      title: 'Ménage',
      subtitle: 'Nettoyage de votre chambre',
      estimate: '15-25 min',
      defaultMinutes: 20,
      icon: 'sparkles',
      iconClass: 'icon-menage',
      serviceCategory: 'SERVICE EN CHAMBRE',
      formTitle: 'Demander le ménage',
      formDesc: 'Notre équipe prend soin de votre chambre selon vos préférences.',
      needs: ['Nettoyage complet', 'Faire le lit', 'Vider les poubelles', 'Réassort salle de bain'],
      placeholder: 'Ex. Merci de ne pas changer les draps...',
    },
    {
      id: 'blanchisserie',
      title: 'Blanchisserie',
      subtitle: 'Collecte et retour en chambre',
      estimate: 'Dans la journée',
      defaultMinutes: 180,
      icon: 'laundry',
      iconClass: 'icon-laundry',
      serviceCategory: 'SERVICE EN CHAMBRE',
      formTitle: 'Demander la blanchisserie',
      formDesc: 'Collecte en chambre et traitement soigné de vos vêtements et textiles.',
      needs: ['Nettoyage à sec', 'Repassage soigné', 'Lavage express', 'Pliage & Pressing délicat'],
      placeholder: 'Ex. Prendre soin de la doublure en soie du costume...',
    },
    {
      id: 'maintenance',
      title: 'Maintenance',
      subtitle: 'Signaler un problème',
      estimate: '10-20 min',
      defaultMinutes: 15,
      icon: 'wrench',
      iconClass: 'icon-maintenance',
      serviceCategory: 'SERVICE EN CHAMBRE',
      formTitle: 'Signaler une maintenance',
      formDesc: 'Intervention technique rapide pour tout équipement de votre chambre.',
      needs: ['Climatisation / Chauffage', 'Télévision & Wi-Fi', 'Plomberie & Eau chaude', 'Éclairage / Serrure'],
      placeholder: 'Ex. La télécommande ne répond plus, piles à changer...',
    },
    {
      id: 'conciergerie',
      title: 'Conciergerie',
      subtitle: 'Conseils et réservations',
      estimate: 'Réponse rapide',
      defaultMinutes: 15,
      icon: 'bell',
      iconClass: 'icon-concierge',
      serviceCategory: 'SERVICE EN CHAMBRE',
      formTitle: 'Contacter la conciergerie',
      formDesc: 'Conseils personnalisés, réservations exclusives et transports privés.',
      needs: ['Réservation restaurant', 'Navette aéroport / Taxi', 'Billetterie spectacle & musée', 'Bagages & Gardiennage'],
      placeholder: 'Ex. Table pour 2 personnes près du Louvre ce soir vers 20h30...',
    },
    {
      id: 'reveil',
      title: 'Service de réveil',
      subtitle: 'Programmer un appel',
      estimate: 'À l\'heure choisie',
      defaultMinutes: 10,
      icon: 'clock',
      iconClass: 'icon-reveil',
      serviceCategory: 'SERVICE EN CHAMBRE',
      formTitle: 'Programmer un réveil',
      formDesc: 'Notre réceptionniste vous appelle personnellement à l\'heure convenue.',
      needs: ['Appel téléphonique personnalisé', 'Café expresso en chambre', 'Point météo & vols', 'Rappel 15 min après'],
      placeholder: 'Ex. Merci de sonner fermement ou de rappeler en cas de non-réponse...',
    },
  ];

  // Screen 2 Form State
  selectedServiceCard: ServiceCardDef | null = null;
  selectedTiming: 'asap' | '1h' | 'custom' = 'asap';
  customSelectedTime = '08:30';
  selectedNeeds: string[] = [];
  customNotes = '';

  constructor(
    private auth: AuthService,
    private router: Router,
    private serviceService: HotelServiceService,
    private demandeService: DemandeService
  ) {}

  ngOnInit(): void {
    this.loadBackendData();
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
    return 'Camille';
  }

  get clientFirstName(): string {
    const email = this.currentUser()?.email;
    if (email) {
      const part = email.split('@')[0].split('.')[0];
      return part.charAt(0).toUpperCase() + part.slice(1);
    }
    return 'Camille';
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

  loadBackendData(): void {
    this.isLoading.set(true);

    // 1. Fetch hotel services and map to cards
    this.serviceService.getAll().subscribe({
      next: (svcs) => {
        this.services.set(svcs);
        this.mapServicesToCards(svcs);
      },
      error: (err) => console.warn('Could not load services from backend', err),
    });

    // 2. Fetch demandes for this client
    this.refreshDemandes();
  }

  refreshDemandes(): void {
    const user = this.currentUser();
    const clientId = user?.idUtilisateur || 1;

    this.demandeService.getByClient(clientId).subscribe({
      next: (demandesList) => {
        this.demandes.set(demandesList);
        this.isLoading.set(false);

        // Find the most recent active demande
        const active = demandesList.find(
          (d) => d.statut === 'EN_COURS' || d.statut === 'PRISE_EN_CHARGE' || d.statut === 'EN_ATTENTE'
        ) || demandesList[0];

        if (active) {
          this.activeDemandeDisplay = {
            idDemande: active.idDemande,
            reference: active.referenceDemande || `#${active.idDemande}`,
            title: active.service?.nom || 'Demande de service',
            status: active.statut,
            statusLabel: this.getStatutLabel(active.statut),
            etaText: active.tempsTraitementEstime ? `~ ${active.tempsTraitementEstime} min` : '~ 15 min',
            dateCreation: active.dateCreation,
          };
        }
      },
      error: (err) => {
        console.warn('Could not load client demandes', err);
        this.isLoading.set(false);
      },
    });
  }

  private mapServicesToCards(svcs: Service[]): void {
    for (const card of this.serviceCards) {
      let match: Service | undefined;
      if (card.id === 'room-service') {
        match = svcs.find((s) => s.nom.toLowerCase().includes('room') || s.idService === 1);
      } else if (card.id === 'menage') {
        match = svcs.find((s) => s.nom.toLowerCase().includes('ménage') || s.idService === 3);
      } else if (card.id === 'blanchisserie') {
        match = svcs.find((s) => s.nom.toLowerCase().includes('blanchisserie') || s.idService === 4);
      } else if (card.id === 'maintenance') {
        match = svcs.find((s) => s.nom.toLowerCase().includes('technique') || s.nom.toLowerCase().includes('maintenance') || s.idService === 7);
      } else if (card.id === 'conciergerie') {
        match = svcs.find((s) => s.nom.toLowerCase().includes('navette') || s.nom.toLowerCase().includes('concierge') || s.idService === 5);
      } else if (card.id === 'reveil') {
        match = svcs.find((s) => s.nom.toLowerCase().includes('réveil') || s.idService === 6);
      }

      if (match && match.idService) {
        card.backendId = match.idService;
      }
    }
  }

  // Navigation handlers
  openServiceRequest(card: ServiceCardDef): void {
    this.selectedServiceCard = card;
    this.selectedTiming = 'asap';
    this.customNotes = '';

    // Pre-select default options for Ménage (matching screenshot 2)
    if (card.id === 'menage') {
      this.selectedNeeds = ['Nettoyage complet', 'Faire le lit'];
    } else {
      this.selectedNeeds = card.needs.length > 0 ? [card.needs[0]] : [];
    }

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

  // Submit Demande
  submitDemand(): void {
    if (!this.selectedServiceCard) return;

    this.isSubmitting.set(true);

    const refNum = Math.floor(2000 + Math.random() * 8000);
    const reference = `#${refNum}`;
    const clientId = this.currentUser()?.idUtilisateur || 1;
    const serviceId = this.selectedServiceCard.backendId || 1;

    let delayMinutes = this.selectedServiceCard.defaultMinutes;
    if (this.selectedTiming === '1h') delayMinutes = 60;

    const dto: CreateDemandeDto = {
      serviceId: serviceId,
      clientId: clientId,
      priorite: this.selectedTiming === 'asap' ? 'HAUTE' : 'NORMALE',
      referenceDemande: reference,
      tempsTraitementEstime: delayMinutes,
    };

    this.demandeService.create(dto).subscribe({
      next: (created) => {
        this.isSubmitting.set(false);

        // Update active demand banner immediately
        const titleDetails = this.selectedNeeds.length > 0
          ? this.selectedNeeds.join(', ')
          : this.selectedServiceCard!.title;

        this.activeDemandeDisplay = {
          idDemande: created.idDemande,
          reference: created.referenceDemande || reference,
          title: titleDetails,
          status: created.statut || 'EN_ATTENTE',
          statusLabel: 'En attente de prise en charge',
          etaText: `~ ${delayMinutes} min`,
          notes: this.customNotes,
        };

        this.toastMessage.set(`Votre demande ${reference} a bien été transmise à notre équipe !`);
        this.currentView = 'hub';
        this.refreshDemandes();

        setTimeout(() => this.toastMessage.set(''), 6000);
      },
      error: (err) => {
        console.error('Erreur lors de la création de la demande', err);
        this.isSubmitting.set(false);

        // Fallback local update so the UI still displays the requested demand
        const titleDetails = this.selectedNeeds.length > 0
          ? this.selectedNeeds.join(', ')
          : this.selectedServiceCard!.title;

        this.activeDemandeDisplay = {
          reference: reference,
          title: titleDetails,
          status: 'EN_ATTENTE',
          statusLabel: 'En attente de prise en charge',
          etaText: `~ ${delayMinutes} min`,
          notes: this.customNotes,
        };

        this.toastMessage.set(`Demande ${reference} enregistrée avec succès.`);
        this.currentView = 'hub';
        setTimeout(() => this.toastMessage.set(''), 6000);
      },
    });
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
        return 'En attente de confirmation';
    }
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

  openActiveDemandeModal(demand: ActiveDemandeDisplay): void {
    this.selectedModalDemande = demand;
    this.showDemandDetailModal = true;
  }

  closeDemandModal(): void {
    this.showDemandDetailModal = false;
    this.selectedModalDemande = null;
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
