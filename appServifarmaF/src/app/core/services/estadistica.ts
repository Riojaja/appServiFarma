import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface Resumen {
  totalVentas: number;
  totalTransacciones: number;
  ticketPromedio: number;
  fechaInicio: string;
  fechaFin: string;
  inicioSemana?: string;
  finSemana?: string;
  mes?: number;
  anio?: number;
}

export interface DistribucionPago {
  medioPago: string;
  total: number;
  porcentaje?: number;
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

  obtenerResumenDiario(fecha?: string): Observable<Resumen> {
    const url = fecha ? `${this.apiUrl}/resumen-diario/${fecha}` : `${this.apiUrl}/resumen-diario`;
    return this.http.get<Resumen>(url)
      .pipe(catchError(this.handleError));
  }

  obtenerResumenSemanal(): Observable<Resumen> {
    return this.http.get<Resumen>(`${this.apiUrl}/resumen-semanal`)
      .pipe(catchError(this.handleError));
  }

  obtenerResumenMensual(anio?: number, mes?: number): Observable<Resumen> {
    let url = `${this.apiUrl}/resumen-mensual`;
    if (anio && mes) {
      url += `/${anio}/${mes}`;
    }
    return this.http.get<Resumen>(url)
      .pipe(catchError(this.handleError));
  }

  obtenerDistribucionPagos(inicio: string, fin: string): Observable<[string, number][]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<[string, number][]>(`${this.apiUrl}/distribucion-pagos`, { params })
      .pipe(catchError(this.handleError));
  }

  obtenerDistribucionPagosDiario(): Observable<[string, number][]> {
    return this.http.get<[string, number][]>(`${this.apiUrl}/distribucion-pagos/diario`)
      .pipe(catchError(this.handleError));
  }

  obtenerVentasPorHora(fecha: string): Observable<[number, number][]> {
    return this.http.get<[number, number][]>(`${this.apiUrl}/ventas-por-hora/${fecha}`)
      .pipe(catchError(this.handleError));
  }

  obtenerVentasPorDia(inicio: string, fin: string): Observable<[string, number][]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<[string, number][]>(`${this.apiUrl}/ventas-por-dia`, { params })
      .pipe(catchError(this.handleError));
  }

  obtenerTicketPromedio(inicio: string, fin: string): Observable<number> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<number>(`${this.apiUrl}/ticket-promedio`, { params })
      .pipe(catchError(this.handleError));
  }

  obtenerTotalVentas(inicio: string, fin: string): Observable<number> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<number>(`${this.apiUrl}/total-ventas`, { params })
      .pipe(catchError(this.handleError));
  }

  obtenerTotalTransacciones(inicio: string, fin: string): Observable<number> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<number>(`${this.apiUrl}/total-transacciones`, { params })
      .pipe(catchError(this.handleError));
  }

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