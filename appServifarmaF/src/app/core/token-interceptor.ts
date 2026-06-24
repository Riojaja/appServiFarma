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

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private router: Router) { }

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
        // Si el error es 401 (No autorizado) y no estamos en login, cerrar sesión
        if (error.status === 401 && !req.url.includes('/auth/login')) {
          this.authService.logout();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }
}