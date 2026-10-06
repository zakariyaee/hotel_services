import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/landing/pages/welcome/welcome.component').then(m => m.WelcomeComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'connected',
    loadComponent: () => import('./features/home/pages/connected/connected.component').then(m => m.ConnectedComponent),
  },
  { path: '**', redirectTo: 'login' },
];
