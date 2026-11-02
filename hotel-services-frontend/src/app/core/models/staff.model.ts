import { PrioriteDemande, StatutDemande } from './enums.model';

export interface StaffDemande {
  idDemande: number;
  referenceDemande: string;
  idService: number;
  serviceNom: string;
  serviceCategorie: string;
  serviceDuree: number;
  necessiteSupervision: boolean;
  idDepartement?: number;
  departementNom?: string;
  idClient?: number;
  clientNom: string;
  clientPrenom: string;
  clientEmail: string;
  numChambre: string;
  statut: StatutDemande;
  priorite: PrioriteDemande;
  dateCreation: string;
  datePriseEnCharge?: string;
  dateResolution?: string;
  tempsTraitementEstime: number;
  tempsTraitementReel?: number;
  idEmployeAssigne?: number;
  nomEmployeAssigne?: string;
  posteEmployeAssigne?: string;
  commentairesClient?: string;
  enRetard: boolean;
  retardMinutes: number;
}

export interface StaffMember {
  idUtilisateur: number;
  nom: string;
  prenom: string;
  email: string;
  tel?: string;
  poste: string;
  idDepartement?: number;
  nomDepartement?: string;
  disponible: boolean;
  nbTachesEnCours: number;
}

export interface ReceptionOverview {
  totalDemandes: number;
  demandesEnAttente: number;
  demandesEnCours: number;
  demandesTerminees: number;
  alertesRetard: number;
  totalChambresOccupees: number;
}

export interface RoomGuestOption {
  idClient: number;
  clientNom: string;
  clientPrenom: string;
  clientEmail: string;
  idChambre: number;
  numChambre: string;
  typeChambre?: string;
  idReservation?: number;
}

export interface ReceptionCreateRequest {
  idClient: number;
  idService: number;
  priorite: PrioriteDemande;
  commentairesClient?: string;
}

