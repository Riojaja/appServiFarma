import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  usuario: string;
  contrasena: string;
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  usuario: string;
  nombreCompleto: string;
  rol: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'token';
  private readonly USER_KEY = 'usuario';
  private readonly ROL_KEY = 'rol';

  constructor(private http: HttpClient) { }

  /**
   * Inicia sesión con las credenciales del usuario.
   * @param credentials Objeto con usuario y contraseña.
   * @returns Observable con la respuesta del login.
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          this.setSession(response);
        })
      );
  }

  /**
   * Guarda los datos de la sesión en localStorage.
   * @param authResult Respuesta del login.
   */
  private setSession(authResult: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, authResult.token);
    localStorage.setItem(this.USER_KEY, authResult.usuario);
    localStorage.setItem(this.ROL_KEY, authResult.rol);
  }

  /**
   * Cierra sesión eliminando los datos del localStorage.
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.ROL_KEY);
  }

  /**
   * Obtiene el token JWT almacenado.
   * @returns Token o null si no existe.
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Obtiene el nombre de usuario almacenado.
   * @returns Nombre de usuario o null.
   */
  getUsuario(): string | null {
    return localStorage.getItem(this.USER_KEY);
  }

  /**
   * Obtiene el rol del usuario almacenado.
   * @returns Rol o null.
   */
  getRol(): string | null {
    return localStorage.getItem(this.ROL_KEY);
  }

  /**
   * Verifica si el usuario está autenticado (tiene token).
   * @returns true si hay token, false en caso contrario.
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Verifica si el usuario es administrador.
   * @returns true si el rol es ADMIN, false en caso contrario.
   */
  isAdmin(): boolean {
    return this.getRol()?.toUpperCase() === 'ADMIN';
  }

  /**
   * Verifica si el usuario es vendedor.
   * @returns true si el rol es VENDEDOR, false en caso contrario.
   */
  isVendedor(): boolean {
    return this.getRol()?.toUpperCase() === 'VENDEDOR';
  }
}