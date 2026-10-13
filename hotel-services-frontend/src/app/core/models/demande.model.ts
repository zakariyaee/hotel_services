import { PrioriteDemande, StatutDemande } from './enums.model';
import { Client } from './client.model';
import { Service } from './service.model';
import { Receptionniste } from './employe.model';

export interface Demande {
  idDemande?: number;
  referenceDemande?: string;
  dateCreation?: string;
  dateModification?: string;
  datePriseEnCharge?: string;
  dateDebutTraitement?: string;
  dateResolution?: string;
  dateCloture?: string;
  priorite: PrioriteDemande;
  statut: StatutDemande;
  motifRejet?: string;
  motifAnnulation?: string;
  tempsTraitementEstime?: number;
  tempsTraitementReel?: number;
  client?: Client;
  service?: Service;
  receptionniste?: Receptionniste;
}

export interface CreateDemandeDto {
  serviceId: number;
  clientId?: number;
  priorite: PrioriteDemande;
  referenceDemande?: string;
  tempsTraitementEstime?: number;
}
