export interface LoginResponse {
  idUtilisateur: number;
  email: string;
  role: string;
  token: string;
  idDepartement?: number;
  departementNom?: string;
}
