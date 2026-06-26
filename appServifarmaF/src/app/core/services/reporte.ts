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

  // Reporte DIGEMIT
  generarDigemit(mes: string, formato: string = 'excel'): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/digemit?mes=${mes}&formato=${formato}`, {
      responseType: 'blob'
    });
  }

  // Reporte Rentabilidad
  generarRentabilidad(inicio: string, fin: string, formato: string = 'excel'): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/rentabilidad?inicio=${inicio}&fin=${fin}&formato=${formato}`, {
      responseType: 'blob'
    });
  }

  // Estadísticas (JSON)
  obtenerEstadisticas(inicio: string, fin: string): Observable<EstadisticasVentasResponse> {
    return this.http.get<EstadisticasVentasResponse>(`${this.apiUrl}/estadisticas?inicio=${inicio}&fin=${fin}`);
  }
}