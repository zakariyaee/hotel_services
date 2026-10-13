import { Utilisateur } from './utilisateur.model';
import { Departement } from './departement.model';

export interface Employe extends Utilisateur {
  poste?: string;
  heureDebut?: string;
  heureFin?: string;
  departement?: Departement;
}

export interface Agent extends Employe {
  disponible: boolean;
}

export interface Personnel extends Employe {
  access: boolean;
  niveauAcces?: string;
  actif: boolean;
}

export interface Receptionniste extends Employe {
  posteReception?: string;
  extensionTelephonique?: string;
}
