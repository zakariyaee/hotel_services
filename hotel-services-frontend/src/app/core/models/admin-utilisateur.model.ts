import { Role } from './enums.model';

export interface AdminUtilisateur {
  idUtilisateur: number;
  nom: string;
  prenom: string;
  email: string;
  tel?: string;
  role: Role;
  idDepartement?: number;
  departementNom?: string;
  poste?: string;
}

export interface CreateAdminUtilisateurDto {
  nom: string;
  prenom: string;
  email: string;
  motDePasse?: string;
  tel?: string;
  role: Role;
  idDepartement?: number | null;
  poste?: string;
}

