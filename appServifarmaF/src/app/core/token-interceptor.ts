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
    // ⚠️ Antes se leía localStorage.getItem('token') directamente aquí, en vez
    // de usar authService.getToken(). Eso hacía que, aunque AuthService pase a
    // usar sessionStorage (aislado por pestaña), el interceptor siguiera
    // leyendo el token "global" de localStorage — reintroduciendo el mismo
    // problema de sesiones cruzadas entre pestañas. Ahora ambos usan la misma
    // fuente de verdad (AuthService), que ya está aislada por pestaña.
    const token = this.authService.getToken();

    let authReq = req;

    if (token && token.trim() !== '') {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token.trim()}`
        }
      });
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('❌ Error en la petición:', error.status, req.url, error.error);

        if (error.status === 401 && !req.url.includes('/auth/login')) {
          // logout() ya limpia la sesión de esta pestaña y redirige
          this.authService.logout();
          this.notificacionService.advertencia('Tu sesión expiró. Inicia sesión de nuevo.');
        } else if (error.status === 403) {
          const mensaje = error.error?.mensaje || 'No tienes permisos para realizar esta acción.';
          this.notificacionService.error(mensaje);
        }
        return throwError(() => error);
      })
    );
  }
}