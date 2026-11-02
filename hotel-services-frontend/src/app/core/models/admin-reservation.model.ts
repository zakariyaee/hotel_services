export interface AdminReservation {
  idReservation?: number;
  dateDebut: string;
  dateFin: string;
  statut?: string;
  typeReservation?: string;
  idClient?: number;
  clientNom: string;
  clientPrenom: string;
  clientEmail: string;
  clientTel?: string;
  idChambre: number;
  numChambre?: string;
  typeChambre?: string;
  accessCode?: string;
  qrCodeBase64?: string;
  loginUrl?: string;
}

export interface CreateReservationDto {
  clientNom: string;
  clientPrenom: string;
  clientEmail: string;
  clientTel?: string;
  idChambre: number;
  dateDebut: string;
  dateFin: string;
  typeReservation?: string;
}
