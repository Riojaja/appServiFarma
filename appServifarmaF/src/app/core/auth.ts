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
  // Si el backend devuelve el ID del usuario, agregarlo aquí
  // id?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'token';
  private readonly USER_KEY = 'usuario';
  private readonly ROL_KEY = 'rol';
  private readonly USER_ID_KEY = 'usuarioId'; // Nueva clave para el ID
  private readonly NOMBRE_KEY = 'nombreCompleto'
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
    // Guardar el ID del usuario. Si el backend no lo devuelve, usamos un valor por defecto (1)
    // Para pruebas, asignamos 1. En producción, si el backend envía el ID, usar authResult.id
    const userId = (authResult as any).id ? (authResult as any).id : 1;
    localStorage.setItem(this.USER_ID_KEY, userId.toString());
    localStorage.setItem(this.NOMBRE_KEY, authResult.nombreCompleto);
  }

  /**
   * Cierra sesión eliminando los datos del localStorage.
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.ROL_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
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
   * Obtiene el ID del usuario almacenado.
   * @returns ID del usuario o null.
   */
  getUsuarioId(): string | null {
    return localStorage.getItem(this.USER_ID_KEY);
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

  getNombreCompleto(): string | null {
  return localStorage.getItem(this.NOMBRE_KEY);
}
}