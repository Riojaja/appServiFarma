// src/app/core/services/usuario.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Usuario } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private apiUrl = `${environment.apiUrl}/usuarios`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl);
  }

  obtener(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/${id}`);
  }

  crear(data: any): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, data);
  }

  actualizar(id: number, data: any): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  cambiarEstado(id: number, activo: boolean): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/estado?activo=${activo}`, {});
  }

  cambiarContrasena(id: number, nuevaContrasena: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/contrasena?nuevaContrasena=${encodeURIComponent(nuevaContrasena)}`, {});
  }

  /** Cierra todas las sesiones activas de un usuario (solo admin) */
  cerrarSesiones(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/cerrar-sesion`, {});
  }

  /** Ejecuta cierre de sesiones por turno (solo admin) */
  cerrarSesionesTurno(): Observable<any> {
    return this.http.post(`${this.apiUrl}/cerrar-sesiones-turno`, {});
  }
}