export interface AdminStatistics {
  totalDemandes: number;
  demandesEnAttente: number;
  demandesEnCours: number;
  demandesTerminees: number;
  tempsMoyenTraitement: number;
  demandesParDepartement: Record<string, number>;
  demandesParService: Record<string, number>;
}
