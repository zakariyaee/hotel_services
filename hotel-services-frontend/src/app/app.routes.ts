import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

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
  // --- CLIENT ---
  {
    path: 'client',
    canActivate: [authGuard, roleGuard('CLIENT')],
    loadComponent: () => import('./features/client/client-dashboard.component').then(m => m.ClientDashboardComponent),
  },
  // --- STAFF: 3 routes distinctes par profil ---
  {
    path: 'staff/employe',
    canActivate: [authGuard, roleGuard(['EMPLOYE', 'STAFF', 'ADMIN'])],
    loadComponent: () => import('./features/staff/employe/employe-dashboard.component').then(m => m.EmployeDashboardComponent),
  },
  {
    path: 'staff/personnel',
    canActivate: [authGuard, roleGuard(['PERSONNEL', 'ADMIN'])],
    loadComponent: () => import('./features/staff/personnel/personnel-dashboard.component').then(m => m.PersonnelDashboardComponent),
  },
  {
    path: 'staff/reception',
    canActivate: [authGuard, roleGuard(['RECEPTIONNISTE', 'ADMIN'])],
    loadComponent: () => import('./features/staff/reception/reception-dashboard.component').then(m => m.ReceptionDashboardComponent),
  },
  // Ancienne route /staff (redirect pour compatibilité)
  {
    path: 'staff',
    canActivate: [authGuard],
    loadComponent: () => import('./features/staff/staff-redirect.component').then(m => m.StaffRedirectComponent),
  },
  // --- ADMIN ---
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard('ADMIN')],
    loadComponent: () => import('./features/home/pages/connected/connected.component').then(m => m.ConnectedComponent),
  },
  { path: 'connected', pathMatch: 'full', redirectTo: 'client' },
  { path: '**', redirectTo: 'login' },
];
