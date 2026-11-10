import { Departement } from './departement.model';

export interface Service {
  idService?: number;
  nom: string;
  description?: string;
  categorie?: string;
  dureeEstimee: number;
  prixIndicatif?: number;
  disponibilite: boolean;
  necessiteSupervision?: boolean;
  imageUrl?: string;
  idDepartement?: number;
  nomDepartement?: string;
  departement?: Departement;
}
