import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EstadisticaService {
  private apiUrl = `${environment.apiUrl}/estadisticas`;

  constructor(private http: HttpClient) { }

  // Resumen diario
  obtenerResumenDiario(fecha?: string): Observable<any> {
    if (fecha) {
      return this.http.get(`${this.apiUrl}/resumen-diario/${fecha}`);
    }
    return this.http.get(`${this.apiUrl}/resumen-diario`);
  }

  // Resumen semanal
  obtenerResumenSemanal(): Observable<any> {
    return this.http.get(`${this.apiUrl}/resumen-semanal`);
  }

  // Resumen mensual
  obtenerResumenMensual(anio?: number, mes?: number): Observable<any> {
    if (anio && mes) {
      return this.http.get(`${this.apiUrl}/resumen-mensual/${anio}/${mes}`);
    }
    return this.http.get(`${this.apiUrl}/resumen-mensual`);
  }

  // Distribución por medios de pago
  obtenerDistribucionPagos(inicio: string, fin: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/distribucion-pagos?inicio=${inicio}&fin=${fin}`);
  }

  /** Distribución de medios de pago, pero solo del día actual (sin rango de fechas). */
  obtenerDistribucionPagosDiario(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/distribucion-pagos/diario`);
  }

  // Ventas por hora
  obtenerVentasPorHora(fecha: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/ventas-por-hora/${fecha}`);
  }

  // Ventas por día
  obtenerVentasPorDia(inicio: string, fin: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/ventas-por-dia?inicio=${inicio}&fin=${fin}`);
  }

  // Ticket promedio
  obtenerTicketPromedio(inicio: string, fin: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/ticket-promedio?inicio=${inicio}&fin=${fin}`);
  }

  // Total ventas
  obtenerTotalVentas(inicio: string, fin: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/total-ventas?inicio=${inicio}&fin=${fin}`);
  }

  // Total transacciones
  obtenerTotalTransacciones(inicio: string, fin: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/total-transacciones?inicio=${inicio}&fin=${fin}`);
  }
}