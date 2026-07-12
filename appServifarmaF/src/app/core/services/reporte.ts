import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ReporteRentabilidad } from '../models/rentabilidad.model';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  /** URL base del API de reportes */
  private apiUrl = `${environment.apiUrl}/reportes`;

  constructor(private http: HttpClient) { }

  // ============================================================
  // REPORTE DE RENTABILIDAD (devuelve JSON)
  // ============================================================


  /**
   * Obtiene los datos de rentabilidad en formato JSON para mostrar en el panel.
   * @param inicio Fecha de inicio (formato: YYYY-MM-DD)
   * @param fin Fecha de fin (formato: YYYY-MM-DD)
   * @returns Observable con el objeto ReporteRentabilidad
   */
  obtenerRentabilidad(inicio: string, fin: string): Observable<ReporteRentabilidad> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin);
    return this.http.get<ReporteRentabilidad>(`${this.apiUrl}/rentabilidad/json`, { params })
      .pipe(catchError(this.handleError));
  }
  /**
   * Exporta el reporte de rentabilidad en el formato solicitado (Excel, PDF o CSV).
   * @param inicio Fecha de inicio (formato: YYYY-MM-DD)
   * @param fin Fecha de fin (formato: YYYY-MM-DD)
   * @param formato Formato de exportación: 'excel', 'pdf' o 'csv' (por defecto 'excel')
   * @returns Observable con el archivo en formato Blob
   */
  exportarRentabilidad(inicio: string, fin: string, formato: string = 'excel'): Observable<Blob> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin)
      .set('formato', formato);
    return this.http.get(`${this.apiUrl}/rentabilidad`, {
      params: params,
      responseType: 'blob'
    }).pipe(catchError(this.handleError));
  }

  // ============================================================
  // REPORTE DIGEMID
  // ============================================================

  /**
   * Genera y exporta el reporte mensual DIGEMID en el formato solicitado.
   * @param mes Mes del reporte (formato: YYYY-MM)
   * @param formato Formato de exportación: 'excel', 'pdf' o 'csv' (por defecto 'excel')
   * @returns Observable con el archivo en formato Blob
   */
  generarDigemit(mes: string, formato: string = 'excel'): Observable<Blob> {
    const params = new HttpParams()
      .set('mes', mes)
      .set('formato', formato);
    return this.http.get(`${this.apiUrl}/digemit`, {
      params: params,
      responseType: 'blob'
    }).pipe(catchError(this.handleError));
  }

  // ============================================================
  // ESTADÍSTICAS DE VENTAS
  // ============================================================

  /**
   * Exporta estadísticas de ventas para un rango de fechas.
   * @param inicio Fecha de inicio (formato: YYYY-MM-DDTHH:mm:ss)
   * @param fin Fecha de fin (formato: YYYY-MM-DDTHH:mm:ss)
   * @param formato Formato de exportación: 'excel', 'pdf' o 'csv' (por defecto 'excel')
   * @returns Observable con el archivo en formato Blob
   */
  generarEstadisticas(inicio: string, fin: string, formato: string = 'excel'): Observable<Blob> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin)
      .set('formato', formato);
    return this.http.get(`${this.apiUrl}/estadisticas`, {
      params: params,
      responseType: 'blob'
    }).pipe(catchError(this.handleError));
  }

  /**
   * Exporta estadísticas de ventas para un período relativo (día, semana o mes).
   * @param periodo Tipo de período: 'dia', 'semana' o 'mes'
   * @param fechaReferencia Fecha de referencia (formato: YYYY-MM-DD)
   * @param formato Formato de exportación: 'excel', 'pdf' o 'csv' (por defecto 'excel')
   * @returns Observable con el archivo en formato Blob
   */
  generarEstadisticasPorPeriodo(periodo: string, fechaReferencia: string, formato: string = 'excel'): Observable<Blob> {
    const params = new HttpParams()
      .set('periodo', periodo)
      .set('fechaReferencia', fechaReferencia)
      .set('formato', formato);
    return this.http.get(`${this.apiUrl}/estadisticas/periodo`, {
      params: params,
      responseType: 'blob'
    }).pipe(catchError(this.handleError));
  }

  // ============================================================
  // MÉTODOS ADICIONALES (OPCIONALES)
  // ============================================================

  /**
   * (Opcional) Obtiene el reporte DIGEMID en formato JSON si el backend lo soporta.
   * @param mes Mes del reporte (formato: YYYY-MM)
   * @returns Observable con el objeto ReporteDigemit
   */
  // obtenerDigemitJson(mes: string): Observable<ReporteDigemit> {
  //   const params = new HttpParams().set('mes', mes);
  //   return this.http.get<ReporteDigemit>(`${this.apiUrl}/digemit/json`, { params })
  //     .pipe(catchError(this.handleError));
  // }

  // ============================================================
  // MANEJO CENTRALIZADO DE ERRORES
  // ============================================================

  /**
   * Maneja errores de las peticiones HTTP.
   * @param error Error capturado
   * @returns Observable con error lanzado
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'Error al generar el reporte.';

    if (error.error instanceof ErrorEvent) {
      // Error del lado del cliente (red, etc.)
      errorMessage = `Error de red: ${error.error.message}`;
      console.error('Error de red:', error.error.message);
    } else {
      // El backend devolvió un código de error HTTP
      errorMessage = error.error?.mensaje || error.message || errorMessage;
      console.error(`Error HTTP ${error.status}:`, error.error || error.message);
    }

    // Puedes integrar aquí un servicio de notificaciones si lo tienes
    // this.notificationService.error(errorMessage);

    return throwError(() => new Error(errorMessage));
  }
}