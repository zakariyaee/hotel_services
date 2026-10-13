import { StatutNotification } from './enums.model';
import { Utilisateur } from './utilisateur.model';
import { Demande } from './demande.model';

export interface Notification {
  idNotification?: number;
  titre: string;
  message: string;
  dateCreation?: string;
  dateEnvoi?: string;
  dateLecture?: string;
  statutEnvoi: StatutNotification;
  utilisateur?: Utilisateur;
  demande?: Demande;
}
