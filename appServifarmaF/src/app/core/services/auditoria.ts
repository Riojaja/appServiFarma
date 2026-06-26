import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuditoriaService {
  private apiUrl = `${environment.apiUrl}/auditoria`;

  constructor(private http: HttpClient) { }

  // Movimientos de stock por usuario
  obtenerMovimientosPorUsuario(usuarioId: number, inicio: string, fin: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/movimientos/usuario/${usuarioId}?inicio=${inicio}&fin=${fin}`);
  }

  // Movimientos de stock por tipo
  obtenerMovimientosPorTipo(tipo: string, inicio: string, fin: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/movimientos/tipo/${tipo}?inicio=${inicio}&fin=${fin}`);
  }

  // Historial de movimientos de un lote
  obtenerHistorialLote(loteId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/movimientos/lote/${loteId}`);
  }

  // Ventas anuladas
  obtenerVentasAnuladas(inicio: string, fin: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/ventas/anuladas?inicio=${inicio}&fin=${fin}`);
  }

  // Ventas por usuario
  obtenerVentasPorUsuario(usuarioId: number, inicio: string, fin: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/ventas/usuario/${usuarioId}?inicio=${inicio}&fin=${fin}`);
  }

  // Historial de caja por usuario
  obtenerHistorialCajaPorUsuario(usuarioId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/caja/usuario/${usuarioId}`);
  }

  // Cajas con diferencia
  obtenerCajasConDiferencia(inicio: string, fin: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/caja/diferencias?inicio=${inicio}&fin=${fin}`);
  }

  // Resumen de actividad
  obtenerResumenActividad(inicio: string, fin: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/resumen-actividad?inicio=${inicio}&fin=${fin}`);
  }
}