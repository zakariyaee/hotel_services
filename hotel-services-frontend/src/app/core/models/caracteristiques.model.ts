import { Service } from './service.model';

export interface Caracteristiques {
  idCaracteristique?: number;
  nom: string;
  description?: string;
  typeValeur?: string;
  valeurParDefaut?: string;
  valeurMinimale?: number;
  valeurMaximale?: number;
  unite?: string;
}

export interface CaracteristiquesServices {
  idCaracteristiqueService?: number;
  valeur?: string;
  service?: Service;
  caracteristique?: Caracteristiques;
}
