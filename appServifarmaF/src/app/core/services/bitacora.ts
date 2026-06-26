import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BitacoraComunicacion, BitacoraComunicacionRequest } from '../models/bitacora-comunicacion.model';

@Injectable({
  providedIn: 'root'
})
export class BitacoraService {
  private apiUrl = `${environment.apiUrl}/bitacora-comunicacion`;

  constructor(private http: HttpClient) { }

  // Crear mensaje
  crear(request: BitacoraComunicacionRequest): Observable<BitacoraComunicacion> {
    return this.http.post<BitacoraComunicacion>(this.apiUrl, request);
  }

  // Marcar como leído
  marcarLeido(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/leido`, {});
  }

  // Obtener por ID
  obtener(id: number): Observable<BitacoraComunicacion> {
    return this.http.get<BitacoraComunicacion>(`${this.apiUrl}/${id}`);
  }

  // Listar todos
  listar(): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(this.apiUrl);
  }

  // Listar no leídos
  listarNoLeidos(): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(`${this.apiUrl}/no-leidos`);
  }

  // Listar por usuario
  listarPorUsuario(usuarioId: number): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(`${this.apiUrl}/usuario/${usuarioId}`);
  }

  // Listar por tipo
  listarPorTipo(tipo: string): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(`${this.apiUrl}/tipo/${tipo}`);
  }

  // Listar por fechas
  listarPorFecha(inicio: string, fin: string): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(`${this.apiUrl}/fechas?inicio=${inicio}&fin=${fin}`);
  }

  // Listar no leídos por usuario
  listarNoLeidosPorUsuario(usuarioId: number): Observable<BitacoraComunicacion[]> {
    return this.http.get<BitacoraComunicacion[]>(`${this.apiUrl}/usuario/${usuarioId}/no-leidos`);
  }

  // Eliminar
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}