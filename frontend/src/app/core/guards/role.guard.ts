import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';

import { NombreRol } from '../models/usuario.model';
import { AuthService } from '../services/auth.service';


export const roleGuard: CanActivateFn = function(route: ActivatedRouteSnapshot) {
  const authService = inject(AuthService);
  const router = inject(Router);

  const rolesPermitidos = route.data['roles'] as NombreRol[] | undefined;

  if (!rolesPermitidos || rolesPermitidos.length === 0) {
    return true;
  }

  if (authService.tieneAlgunRol(rolesPermitidos)) {
    return true;
  }

  return router.createUrlTree(['/panel']);
};
