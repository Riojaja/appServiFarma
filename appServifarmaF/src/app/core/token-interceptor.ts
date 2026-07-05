import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth';
import { Router } from '@angular/router';
import { NotificacionService } from './services/notificacion';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificacionService: NotificacionService
  ) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Obtener el token del servicio de autenticación
    const token = this.authService.getToken();
    let authReq = req;

    // Si hay token, clonar la petición y agregar el header Authorization
    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    // Continuar con la petición y manejar errores
    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        // 401: sesión inválida o expirada -> cerrar sesión y redirigir a login
        if (error.status === 401 && !req.url.includes('/auth/login')) {
          this.authService.logout();
          this.router.navigate(['/login']);
          this.notificacionService.advertencia('Tu sesión expiró. Inicia sesión de nuevo.');
        }
        // 403: el backend rechazó la acción por rol/permisos (@PreAuthorize)
        else if (error.status === 403) {
          const mensaje = error.error?.mensaje || 'No tienes permisos para realizar esta acción.';
          this.notificacionService.error(mensaje);
        }
        return throwError(() => error);
      })
    );
  }
}