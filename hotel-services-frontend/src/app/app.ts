import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { ServiceTypeService } from './core/services/service-type.service';
import { ServiceType } from './core/models/service-type.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  services = signal<ServiceType[]>([]);

  constructor(private serviceTypeService: ServiceTypeService) {}

  ngOnInit(): void {
    console.log("Initialisation du composant, appel de l'API...");
    this.serviceTypeService.getAll().subscribe({
      next: (data) => {
        console.log("Données reçues :", data);
        this.services.set(data);
      },
      error: (err) => console.error('Erreur API :', err)
    });
  }
}
