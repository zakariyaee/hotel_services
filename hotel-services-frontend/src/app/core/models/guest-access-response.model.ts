export interface GuestAccessResponse {
  idUtilisateur: number;
  email: string;
  nom: string;
  prenom: string;
  role: string;
  token: string;
  idReservation: number;
  numChambre: string;
  typeChambre: string;
  dateDebut: string;
  dateFin: string;
}

