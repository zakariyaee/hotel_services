import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard qui vérifie un ou plusieurs rôles autorisés.
 * Usage: canActivate: [roleGuard('ADMIN')] ou canActivate: [roleGuard(['EMPLOYE', 'ADMIN'])]
 */
export const roleGuard = (requiredRoles: string | string[]): CanActivateFn => {
  return (_route, _state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isLoggedIn()) {
      router.navigate(['/login']);
      return false;
    }

    const currentRole = (authService.getRole() || '').toUpperCase();
    const rolesArray = Array.isArray(requiredRoles)
      ? requiredRoles.map(r => r.toUpperCase())
      : [requiredRoles.toUpperCase()];

    if (rolesArray.includes(currentRole) || currentRole === 'ADMIN') {
      return true;
    }

    // Redirige vers la page appropriée si le rôle ne correspond pas
    const roleRoutes: Record<string, string> = {
      CLIENT: '/client',
      EMPLOYE: '/staff/employe',
      PERSONNEL: '/staff/personnel',
      RECEPTIONNISTE: '/staff/reception',
      STAFF: '/staff/employe',
      ADMIN: '/admin',
    };

    const redirect = roleRoutes[currentRole] ?? '/login';
    router.navigate([redirect]);
    return false;
  };
};
