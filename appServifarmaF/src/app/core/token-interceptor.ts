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
    const token = localStorage.getItem('token');
    
    console.log('🔑 Token obtenido:', token ? '✅ Existe' : '❌ No existe');
    console.log('📤 URL de la petición:', req.url);

    let authReq = req;

    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('✅ Header Authorization agregado');
    } else {
      console.warn('⚠️ No hay token, request sin autorización');
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Error en la petición:', error.status, error.message);
        console.error('❌ URL:', req.url);
        console.error('❌ Body del error:', error.error);

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
          // ⚠️ IMPORTANTE: NO hacer logout aquí, solo mostrar error.
          // Si el backend invalida el token, el siguiente 401 hará logout.
        }
        return throwError(() => error);
      })
    );
  }
}