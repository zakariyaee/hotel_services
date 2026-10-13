import { Departement } from './departement.model';

export interface Service {
  idService?: number;
  nom: string;
  description?: string;
  dureeEstimee: number;
  disponibilite: boolean;
  imageUrl?: string;
  departement?: Departement;
}
