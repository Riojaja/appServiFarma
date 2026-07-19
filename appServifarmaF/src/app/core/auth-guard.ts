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
    // ✅ 1. Verificar autenticación
    const token = this.authService.getToken();
    if (!token) {
      console.warn('🔒 No hay token. Redirigiendo al login.');
      this.router.navigate(['/login']);
      return false;
    }

    // ✅ 2. Verificar roles si se especifican
    const requiredRoles = route.data['roles'] as string[];
    if (requiredRoles && requiredRoles.length > 0) {
      const userRole = this.authService.getRol()?.toLowerCase() || '';
      const hasRequiredRole = requiredRoles.some(role => role.toLowerCase() === userRole);
      if (!hasRequiredRole) {
        console.warn('🔒 Rol no autorizado. Redirigiendo al dashboard.');
        this.router.navigate(['/dashboard']);
        return false;
      }
    }

    return true;
  }
}