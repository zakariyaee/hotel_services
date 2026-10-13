import { StatutReservation } from './enums.model';
import { Client } from './client.model';
import { Chambre } from './chambre.model';

export interface Reservation {
  idReservation?: number;
  typeReservation?: string;
  dateDebut: string;
  dateFin: string;
  statut: StatutReservation;
  client?: Client;
  chambre?: Chambre;
}

export interface ClientAccessToken {
  tokenId?: number;
  tokenHash: string;
  dateCreation: string;
  dateExpiration: string;
  revoked: boolean;
  nbrUtilisations: number;
  reservation?: Reservation;
}
