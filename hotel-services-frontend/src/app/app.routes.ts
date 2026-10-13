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
  { path: 'client', loadComponent: () => import('./features/client/client-dashboard.component').then(m => m.ClientDashboardComponent) },
  { path: 'staff', loadComponent: () => import('./features/staff/staff-dashboard.component').then(m => m.StaffDashboardComponent) },
  { path: 'admin', loadComponent: () => import('./features/home/pages/connected/connected.component').then(m => m.ConnectedComponent) },
  { path: 'connected', pathMatch: 'full', redirectTo: 'client' },
  { path: '**', redirectTo: 'login' },
];
