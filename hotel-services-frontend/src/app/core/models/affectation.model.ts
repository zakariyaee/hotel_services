import { Receptionniste, Agent } from './employe.model';
import { Demande } from './demande.model';

export interface Affectation {
  idAffectation?: number;
  dateAffectation?: string;
  receptionniste?: Receptionniste;
  agent?: Agent;
  demande?: Demande;
}
