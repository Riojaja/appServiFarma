// bitacora.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { BitacoraComunicacion, BitacoraComunicacionRequest } from '../models/bitacora-comunicacion.model';

@Injectable({
  providedIn: 'root'
})
export class BitacoraService {
  private apiUrl = `${environment.apiUrl}/bitacora-comunicacion`;

  constructor(private http: HttpClient) { }

  // ==============================
  // OPERACIONES DE CREACIÓN Y ACTUALIZACIÓN
  // ==============================

  crear(request: BitacoraComunicacionRequest): Observable<BitacoraComunicacion> {
    return this.http.post<BitacoraComunicacion>(this.apiUrl, request)
      .pipe(catchError(this.handleError));
  }

  marcarLeido(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/leido`, {})
      .pipe(catchError(this.handleError));
  }

  // ==============================
  // CONSULTAS
  // ==============================

  obtener(id: number): Observable<BitacoraComunicacion> {
    return this.http.get<BitacoraComunicacion>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  listar(): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  listarNoLeidos(): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(`${this.apiUrl}/no-leidos`)
      .pipe(catchError(this.handleError));
  }

  listarPorUsuario(usuarioId: number): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(`${this.apiUrl}/usuario/${usuarioId}`)
      .pipe(catchError(this.handleError));
  }

  listarPorTipo(tipo: string): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(`${this.apiUrl}/tipo/${tipo}`)
      .pipe(catchError(this.handleError));
  }

  listarPorFecha(inicio: Date | string, fin: Date | string): Observable<BitacoraComunicacion[]> {
    let params = new HttpParams()
      .set('inicio', typeof inicio === 'string' ? inicio : inicio.toISOString())
      .set('fin', typeof fin === 'string' ? fin : fin.toISOString());
    return this.http.get<BitacoraComunicacion[]>(`${this.apiUrl}/fechas`, { params })
      .pipe(catchError(this.handleError));
  }

  listarNoLeidosPorUsuario(usuarioId: number): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(`${this.apiUrl}/usuario/${usuarioId}/no-leidos`)
      .pipe(catchError(this.handleError));
  }

  // ==============================
  // OPERACIONES DE ELIMINACIÓN
  // ==============================

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  // ==============================
  // MÉTODOS ADICIONALES ÚTILES
  // ==============================

  /** Contar mensajes no leídos de un usuario (puedes implementar un endpoint específico o calcularlo en el frontend) */
  contarNoLeidos(usuarioId: number): Observable<number> {
    return this.listarNoLeidosPorUsuario(usuarioId).pipe(
      map(mensajes => mensajes.length)
    );
  }

  // ==============================
  // MANEJO DE ERRORES
  // ==============================

  private handleError(error: any): Observable<never> {
    console.error('Error en BitacoraService:', error);
    // Puedes personalizar el mensaje de error según el status
    return throwError(() => new Error(error.message || 'Error al comunicarse con el servidor.'));
  }
}