import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) { }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    // 1. Verificar autenticación
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return false;
    }

    // 2. Verificar roles si se especifican en la ruta
    const requiredRoles = route.data['roles'] as string[];
    if (requiredRoles && requiredRoles.length > 0) {
      const userRole = this.authService.getRol()?.toLowerCase(); // ✅ Convertir a minúsculas
      // También soportar 'administrador'
      const normalizedUserRole = userRole === 'administrador' ? 'admin' : userRole;
      
      if (!normalizedUserRole || !requiredRoles.includes(normalizedUserRole)) {
        this.router.navigate(['/dashboard']);
        return false;
      }
    }

    return true;
  }
}