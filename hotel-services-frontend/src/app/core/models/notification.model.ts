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

// ─── Modèles Temps Réel (WebSocket natif) ───────────────────────────────────

export type WsNotificationType =
  | 'NOUVELLE_DEMANDE'
  | 'DEMANDE_CREEE_CLIENT'
  | 'TACHE_PRISE_EN_CHARGE'
  | 'TACHE_PRISE_EN_CHARGE_STAFF'
  | 'TACHE_ASSIGNEE_EMPLOYE'
  | 'TACHE_ASSIGNEE_CLIENT'
  | 'TACHE_ASSIGNEE_RECEPTION'
  | 'TACHE_TERMINEE'
  | 'TACHE_TERMINEE_STAFF'
  | 'TACHE_REJETEE'
  | 'TACHE_ANNULEE'
  | 'CONNECTED'
  | 'PONG';

export interface WsNotification {
  idNotification?: number;
  type: WsNotificationType | string;
  titre: string;
  message: string;
  idDemande?: number;
  referenceDemande?: string;
  idService?: number;
  serviceNom?: string;
  idDepartement?: number;
  departementNom?: string;
  idClient?: number;
  clientNom?: string;
  numChambre?: string;
  priorite?: string;
  statut?: string;
  timestamp?: string;
  read?: boolean;
}

/** DTO retourné par GET /api/notifications */
export interface NotificationResponse {
  idNotification: number;
  titre: string;
  message: string;
  dateCreation: string;
  dateLecture?: string;
  statutEnvoi: string;
  idDemande?: number;
  referenceDemande?: string;
  type?: string;
}

