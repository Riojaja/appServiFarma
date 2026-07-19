import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';

export interface LoginRequest {
  usuario: string;
  contrasena: string;
}

export interface AuthResponse {
  id: number;
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
  private readonly USER_ID_KEY = 'usuarioId';
  private readonly NOMBRE_KEY = 'nombreCompleto';

  // ⚠️ CAMBIO IMPORTANTE: se usa sessionStorage en vez de localStorage.
  //
  // localStorage se comparte entre TODAS las pestañas del mismo navegador para
  // el mismo origen. En una botica es común que dos empleados usen la misma
  // computadora en pestañas distintas (ej. el administrador deja su sesión
  // abierta en una pestaña y un vendedor inicia sesión en otra). Con
  // localStorage, el segundo login SOBRESCRIBE el token/rol del primero para
  // TODAS las pestañas — la pestaña del admin sigue viéndose como admin en
  // pantalla, pero cualquier petición que haga ya viaja con el token del
  // vendedor (o al revés). Esto es exactamente lo que causaba que "el
  // vendedor tome las acciones del administrador" de forma intermitente.
  //
  // sessionStorage es exclusivo de cada pestaña/ventana: cada una mantiene su
  // propia sesión de forma aislada, y se limpia sola al cerrar esa pestaña.
  private storage: Storage = sessionStorage;

  constructor(
    private http: HttpClient,
    private router: Router
  ) { }

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
   * Guarda los datos de la sesión (aislados a esta pestaña/ventana).
   * El backend (AuthResponse.java) ya devuelve el id real del usuario
   * desde el login, así que ya no se usa ningún valor por defecto.
   * @param authResult Respuesta del login.
   */
  private setSession(authResult: AuthResponse): void {
    this.storage.setItem(this.TOKEN_KEY, authResult.token);
    this.storage.setItem(this.USER_KEY, authResult.usuario);
    this.storage.setItem(this.ROL_KEY, authResult.rol);
    this.storage.setItem(this.USER_ID_KEY, authResult.id.toString());
    this.storage.setItem(this.NOMBRE_KEY, authResult.nombreCompleto);
  }

  /**
   * Cierra sesión eliminando los datos de esta pestaña/ventana.
   */
  logout(): void {
    this.storage.removeItem(this.TOKEN_KEY);
    this.storage.removeItem(this.USER_KEY);
    this.storage.removeItem(this.ROL_KEY);
    this.storage.removeItem(this.USER_ID_KEY);
    this.storage.removeItem(this.NOMBRE_KEY);
    this.router.navigate(['/login']);
  }

  /**
   * Obtiene el token JWT almacenado.
   * @returns Token o null si no existe.
   */
  getToken(): string | null {
    return this.storage.getItem(this.TOKEN_KEY);
  }

  /**
   * Obtiene el nombre de usuario almacenado.
   * @returns Nombre de usuario o null.
   */
  getUsuario(): string | null {
    return this.storage.getItem(this.USER_KEY);
  }

  /**
   * Obtiene el rol del usuario almacenado.
   * @returns Rol o null.
   */
  getRol(): string | null {
    return this.storage.getItem(this.ROL_KEY);
  }

  /**
   * Obtiene el ID del usuario almacenado.
   * @returns ID del usuario o null.
   */
  getUsuarioId(): string | null {
    return this.storage.getItem(this.USER_ID_KEY);
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
    return this.storage.getItem(this.NOMBRE_KEY);
  }
}