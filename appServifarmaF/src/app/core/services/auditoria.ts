import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

// ============================================================
// IMPORTAR MODELOS DESDE LA CARPETA CORRESPONDIENTE
// ============================================================
import {
  MovimientoStock,
  VentaAuditoria,
  CajaDiferencia,
  HistorialCaja,
  ResumenActividad,
  UsuarioBasico
} from '../models/auditoria.model';

@Injectable({
  providedIn: 'root'
})
export class AuditoriaService {
  private apiUrl = `${environment.apiUrl}/auditoria`;

  constructor(private http: HttpClient) { }

  // ============================================================
  // AUDITORÍA DE MOVIMIENTOS DE STOCK
  // ============================================================

  /**
   * Obtiene movimientos de stock realizados por un usuario en un rango de fechas.
   */
  obtenerMovimientosPorUsuario(usuarioId: number, inicio: string, fin: string): Observable<MovimientoStock[]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<MovimientoStock[]>(`${this.apiUrl}/movimientos/usuario/${usuarioId}`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene movimientos de stock filtrados por tipo en un rango de fechas.
   */
  obtenerMovimientosPorTipo(tipo: string, inicio: string, fin: string): Observable<MovimientoStock[]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<MovimientoStock[]>(`${this.apiUrl}/movimientos/tipo/${tipo}`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene el historial completo de movimientos de un lote específico.
   */
  obtenerHistorialLote(loteId: number): Observable<MovimientoStock[]> {
    return this.http.get<MovimientoStock[]>(`${this.apiUrl}/movimientos/lote/${loteId}`)
      .pipe(catchError(this.handleError));
  }

  // ============================================================
  // AUDITORÍA DE VENTAS
  // ============================================================

  /**
   * Obtiene todas las ventas anuladas en un rango de fechas.
   */
  obtenerVentasAnuladas(inicio: string, fin: string): Observable<VentaAuditoria[]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<VentaAuditoria[]>(`${this.apiUrl}/ventas/anuladas`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene las ventas realizadas por un usuario en un rango de fechas.
   */
  obtenerVentasPorUsuario(usuarioId: number, inicio: string, fin: string): Observable<VentaAuditoria[]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<VentaAuditoria[]>(`${this.apiUrl}/ventas/usuario/${usuarioId}`, { params })
      .pipe(catchError(this.handleError));
  }

  // ============================================================
  // AUDITORÍA DE CAJA
  // ============================================================

  /**
   * Obtiene el historial de aperturas y cierres de caja de un usuario.
   */
  obtenerHistorialCajaPorUsuario(usuarioId: number): Observable<HistorialCaja[]> {
    return this.http.get<HistorialCaja[]>(`${this.apiUrl}/caja/usuario/${usuarioId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene las cajas cerradas que tuvieron diferencia (sobrante o faltante).
   */
  obtenerCajasConDiferencia(inicio: string, fin: string): Observable<CajaDiferencia[]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<CajaDiferencia[]>(`${this.apiUrl}/caja/diferencias`, { params })
      .pipe(catchError(this.handleError));
  }

  // ============================================================
  // RESUMEN DE ACTIVIDAD
  // ============================================================

  /**
   * Obtiene un resumen consolidado de actividad del sistema en un rango de fechas.
   */
  obtenerResumenActividad(inicio: string, fin: string): Observable<ResumenActividad> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<ResumenActividad>(`${this.apiUrl}/resumen-actividad`, { params })
      .pipe(catchError(this.handleError));
  }

  // ============================================================
  // MANEJO DE ERRORES
  // ============================================================

  private handleError(error: any): Observable<never> {
    let errorMessage = 'Error al obtener datos de auditoría.';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error de red: ${error.error.message}`;
      console.error('Error de red:', error.error.message);
    } else {
      errorMessage = error.error?.mensaje || error.message || errorMessage;
      console.error(`Error HTTP ${error.status}:`, error.error || error.message);
    }

    return throwError(() => new Error(errorMessage));
  }
}