import { StatutChambre } from './enums.model';

export interface Chambre {
  idChambre?: number;
  numChambre: string;
  typeChambre?: string;
  etage: number;
  capacite: number;
  statut: StatutChambre;
}
