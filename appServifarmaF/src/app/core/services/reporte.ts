import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ReporteDigemitResponse {
  mes: string;
  fechaGeneracion: string;
  items: any[];
}

export interface ReporteRentabilidadResponse {
  fechaInicio: string;
  fechaFin: string;
  ingresosTotales: number;
  costoVentas: number;
  mermas: number;
  margenBruto: number;
  margenNeto: number;
  categorias: any[];
}

export interface EstadisticasVentasResponse {
  fechaInicio: string;
  fechaFin: string;
  totalVentas: number;
  totalTransacciones: number;
  ticketPromedio: number;
  distribucionMediosPago: any[];
  productosMasVendidos: any[];
  tendenciaDiaria: any[];
  variacionPorcentual: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  private apiUrl = `${environment.apiUrl}/reportes`;

  constructor(private http: HttpClient) { }

  // Reporte DIGEMIT (siempre devuelve un archivo)
  generarDigemit(mes: string, formato: string = 'excel'): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/digemit?mes=${mes}&formato=${formato}`, {
      responseType: 'blob'
    });
  }

  // Reporte Rentabilidad (siempre devuelve un archivo)
  generarRentabilidad(inicio: string, fin: string, formato: string = 'excel'): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/rentabilidad?inicio=${inicio}&fin=${fin}&formato=${formato}`, {
      responseType: 'blob'
    });
  }

  // Estadísticas de ventas por rango de fechas (también devuelve un archivo,
  // no JSON - el backend genera excel/pdf/csv según el parámetro "formato")
  generarEstadisticas(inicio: string, fin: string, formato: string = 'excel'): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/estadisticas?inicio=${inicio}&fin=${fin}&formato=${formato}`, {
      responseType: 'blob'
    });
  }

  // Estadísticas de ventas por período relativo (día/semana/mes) - NUEVO
  // Backend: GET /api/reportes/estadisticas/periodo (no estaba conectado)
  generarEstadisticasPorPeriodo(periodo: string, fechaReferencia: string, formato: string = 'excel'): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/estadisticas/periodo?periodo=${periodo}&fechaReferencia=${fechaReferencia}&formato=${formato}`, {
      responseType: 'blob'
    });
  }
}