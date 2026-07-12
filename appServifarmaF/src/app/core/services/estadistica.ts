import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

// ==============================
// INTERFACES PARA TIPADO
// ==============================
export interface Resumen {
  totalVentas: number;
  totalTransacciones: number;
  ticketPromedio: number;
  fechaInicio: string;
  fechaFin: string;
  // solo para semanal/mensual
  inicioSemana?: string;
  finSemana?: string;
  mes?: number;
  anio?: number;
}

export interface DistribucionPago {
  medioPago: string;
  total: number;
  porcentaje?: number; // opcional, calculado en frontend
}

export interface VentaPorHora {
  hora: number;
  total: number;
}

export interface VentaPorDia {
  fecha: string;
  total: number;
}

@Injectable({
  providedIn: 'root'
})
export class EstadisticaService {
  private apiUrl = `${environment.apiUrl}/estadisticas`;

  constructor(private http: HttpClient) { }

  /**
   * Obtiene el resumen diario (para hoy si no se envía fecha).
   */
  obtenerResumenDiario(fecha?: string): Observable<Resumen> {
    const url = fecha ? `${this.apiUrl}/resumen-diario/${fecha}` : `${this.apiUrl}/resumen-diario`;
    return this.http.get<Resumen>(url)
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene el resumen de la semana actual (lunes a domingo).
   */
  obtenerResumenSemanal(): Observable<Resumen> {
    return this.http.get<Resumen>(`${this.apiUrl}/resumen-semanal`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene el resumen mensual (mes actual si no se envía año/mes).
   */
  obtenerResumenMensual(anio?: number, mes?: number): Observable<Resumen> {
    let url = `${this.apiUrl}/resumen-mensual`;
    if (anio && mes) {
      url += `/${anio}/${mes}`;
    }
    return this.http.get<Resumen>(url)
      .pipe(catchError(this.handleError));
  }

  /**
   * Distribución de medios de pago en un rango de fechas.
   */
  obtenerDistribucionPagos(inicio: string, fin: string): Observable<DistribucionPago[]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<DistribucionPago[]>(`${this.apiUrl}/distribucion-pagos`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Distribución de medios de pago del día actual.
   */
  obtenerDistribucionPagosDiario(): Observable<DistribucionPago[]> {
    return this.http.get<DistribucionPago[]>(`${this.apiUrl}/distribucion-pagos/diario`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Ventas agrupadas por hora para una fecha específica.
   */
  obtenerVentasPorHora(fecha: string): Observable<VentaPorHora[]> {
    return this.http.get<VentaPorHora[]>(`${this.apiUrl}/ventas-por-hora/${fecha}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Ventas agrupadas por día en un rango de fechas.
   */
  obtenerVentasPorDia(inicio: string, fin: string): Observable<VentaPorDia[]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<VentaPorDia[]>(`${this.apiUrl}/ventas-por-dia`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Ticket promedio en un rango de fechas.
   */
  obtenerTicketPromedio(inicio: string, fin: string): Observable<number> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<number>(`${this.apiUrl}/ticket-promedio`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Total de ventas en un rango de fechas.
   */
  obtenerTotalVentas(inicio: string, fin: string): Observable<number> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<number>(`${this.apiUrl}/total-ventas`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Número de transacciones en un rango de fechas.
   */
  obtenerTotalTransacciones(inicio: string, fin: string): Observable<number> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<number>(`${this.apiUrl}/total-transacciones`, { params })
      .pipe(catchError(this.handleError));
  }

  // ============================================================
  // MANEJO CENTRALIZADO DE ERRORES
  // ============================================================

  private handleError(error: any): Observable<never> {
    let errorMessage = 'Ocurrió un error en la petición de estadísticas.';
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