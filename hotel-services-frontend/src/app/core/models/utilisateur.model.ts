import { Role } from './enums.model';

export interface Utilisateur {
  idUtilisateur?: number;
  nom: string;
  prenom: string;
  email: string;
  tel?: string;
  role: Role;
}
