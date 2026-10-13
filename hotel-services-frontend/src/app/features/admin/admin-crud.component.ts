import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HotelServiceService } from '../../core/services/service.service';
import { DepartementService } from '../../core/services/departement.service';
import { Service } from '../../core/models/service.model';
import { Departement } from '../../core/models/departement.model';

@Component({
  selector: 'app-admin-crud',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-crud.component.html',
  styleUrl: './admin-crud.component.scss',
})
export class AdminCrudComponent {
  readonly services = signal<Service[]>([]);
  readonly departements = signal<Departement[]>([]);
  readonly message = signal('');
  serviceId: number | null = null;
  departementId: number | null = null;
  serviceForm = { nom: '', description: '', dureeEstimee: 30, disponibilite: true, imageUrl: '' };
  departementForm = { nom: '', description: '', horairesOuverture: '' };

  constructor(private serviceApi: HotelServiceService, private departementApi: DepartementService) {
    this.reload();
  }

  reload(): void {
    this.serviceApi.getAll().subscribe({ next: data => this.services.set(data) });
    this.departementApi.getAll().subscribe({ next: data => this.departements.set(data) });
  }

  editService(value: Service): void {
    this.serviceId = value.idService ?? null;
    this.serviceForm = {
      nom: value.nom,
      description: value.description ?? '',
      dureeEstimee: value.dureeEstimee,
      disponibilite: value.disponibilite,
      imageUrl: value.imageUrl ?? '',
    };
  }

  saveService(): void {
    const request = this.serviceId === null
      ? this.serviceApi.create(this.serviceForm)
      : this.serviceApi.update(this.serviceId, this.serviceForm);
    request.subscribe({
      next: () => { this.message.set('Service enregistré.'); this.cancelService(); this.reload(); },
      error: () => this.message.set('Impossible d’enregistrer le service.'),
    });
  }

  deleteService(id: number): void {
    if (!confirm('Supprimer ce service ?')) return;
    this.serviceApi.delete(id).subscribe({ next: () => { this.message.set('Service supprimé.'); this.reload(); } });
  }

  cancelService(): void {
    this.serviceId = null;
    this.serviceForm = { nom: '', description: '', dureeEstimee: 30, disponibilite: true, imageUrl: '' };
  }

  editDepartement(value: Departement): void {
    this.departementId = value.idDepartement ?? null;
    this.departementForm = {
      nom: value.nom,
      description: value.description ?? '',
      horairesOuverture: value.horairesOuverture ?? '',
    };
  }

  saveDepartement(): void {
    const request = this.departementId === null
      ? this.departementApi.create(this.departementForm)
      : this.departementApi.update(this.departementId, this.departementForm);
    request.subscribe({
      next: () => { this.message.set('Département enregistré.'); this.cancelDepartement(); this.reload(); },
      error: () => this.message.set('Impossible d’enregistrer le département.'),
    });
  }

  deleteDepartement(id: number): void {
    if (!confirm('Supprimer ce département ?')) return;
    this.departementApi.delete(id).subscribe({ next: () => { this.message.set('Département supprimé.'); this.reload(); } });
  }

  cancelDepartement(): void {
    this.departementId = null;
    this.departementForm = { nom: '', description: '', horairesOuverture: '' };
  }
}
