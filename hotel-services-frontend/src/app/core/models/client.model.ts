import { Utilisateur } from './utilisateur.model';

export interface Client extends Utilisateur {
  reservationStatut: boolean;
  languePreferee?: string;
  preferenceNotification: boolean;
}
