import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard qui vérifie un rôle précis.
 * Usage: canActivate: [roleGuard('ADMIN')]
 */
export const roleGuard = (requiredRole: string): CanActivateFn => {
  return (_route, _state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isLoggedIn()) {
      router.navigate(['/login']);
      return false;
    }

    const role = authService.getRole();
    if (role?.toUpperCase() === requiredRole.toUpperCase()) {
      return true;
    }

    // Redirige vers le dashboard approprié si le rôle ne correspond pas
    const roleRoutes: Record<string, string> = {
      CLIENT: '/client',
      STAFF: '/staff',
      ADMIN: '/admin',
    };
    const redirect = roleRoutes[role?.toUpperCase() ?? ''] ?? '/login';
    router.navigate([redirect]);
    return false;
  };
};

