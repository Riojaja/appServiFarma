import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Lote } from '../models/lote.model';

export interface AjusteStockRequest {
  usuarioId: number;
  cantidad: number;
  tipoMovimiento: 'ajuste' | 'merma';
  observacion: string;
}

@Injectable({
  providedIn: 'root'
})
export class LoteService {
  private apiUrl = `${environment.apiUrl}/lotes`;

  constructor(private http: HttpClient) { }

  // ==================== CRUD CON MAPEO DE IMÁGENES ====================

  listar(): Observable<Lote[]> {
    return this.http.get<Lote[]>(this.apiUrl).pipe(
      map(lotes => lotes.map(lote => ({
        ...lote,
        productoImagen: lote.productoImagen ? this.obtenerUrlCompleta(lote.productoImagen) : undefined
      })))
    );
  }

  obtener(id: number): Observable<Lote> {
    return this.http.get<Lote>(`${this.apiUrl}/${id}`).pipe(
      map(lote => ({
        ...lote,
        productoImagen: lote.productoImagen ? this.obtenerUrlCompleta(lote.productoImagen) : undefined
      }))
    );
  }

  crear(data: Lote): Observable<Lote> {
    return this.http.post<Lote>(this.apiUrl, data);
  }

  actualizar(id: number, data: Lote): Observable<Lote> {
    return this.http.put<Lote>(`${this.apiUrl}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ==================== FILTROS CON MAPEO DE IMÁGENES ====================

  listarPorProducto(productoId: number): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${this.apiUrl}/producto/${productoId}`).pipe(
      map(lotes => lotes.map(lote => ({
        ...lote,
        productoImagen: lote.productoImagen ? this.obtenerUrlCompleta(lote.productoImagen) : undefined
      })))
    );
  }

  listarPorEstado(estado: string): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${this.apiUrl}/estado/${estado}`).pipe(
      map(lotes => lotes.map(lote => ({
        ...lote,
        productoImagen: lote.productoImagen ? this.obtenerUrlCompleta(lote.productoImagen) : undefined
      })))
    );
  }

  buscarPorLote(numero: string): Observable<Lote> {
    return this.http.get<Lote>(`${this.apiUrl}/numero/${numero}`).pipe(
      map(lote => ({
        ...lote,
        productoImagen: lote.productoImagen ? this.obtenerUrlCompleta(lote.productoImagen) : undefined
      }))
    );
  }

  buscarPorLoteContaining(numero: string): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${this.apiUrl}/numero/contiene/${numero}`).pipe(
      map(lotes => lotes.map(lote => ({
        ...lote,
        productoImagen: lote.productoImagen ? this.obtenerUrlCompleta(lote.productoImagen) : undefined
      })))
    );
  }

  obtenerProximosAVencer(dias: number): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${this.apiUrl}/proximos-a-vencer?diasAnticipacion=${dias}`).pipe(
      map(lotes => lotes.map(lote => ({
        ...lote,
        productoImagen: lote.productoImagen ? this.obtenerUrlCompleta(lote.productoImagen) : undefined
      })))
    );
  }

  obtenerVencidos(): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${this.apiUrl}/vencidos`).pipe(
      map(lotes => lotes.map(lote => ({
        ...lote,
        productoImagen: lote.productoImagen ? this.obtenerUrlCompleta(lote.productoImagen) : undefined
      })))
    );
  }

  // ==================== ACCIONES ====================

  marcarDeteriorado(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/deteriorado`, {});
  }

  ajustarStock(id: number, data: AjusteStockRequest): Observable<{ mensaje: string }> {
    return this.http.patch<{ mensaje: string }>(`${this.apiUrl}/${id}/stock`, data);
  }

  actualizarLotesVencidos(): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(`${this.apiUrl}/actualizar-vencidos`, {});
  }

  // ==================== UTILIDAD PRIVADA ====================

  /**
   * Convierte una ruta relativa en URL absoluta usando la URL base del backend.
   * @param ruta Ruta relativa (ej. /uploads/productos/...)
   * @returns URL absoluta (ej. http://localhost:8080/uploads/productos/...)
   */
  private obtenerUrlCompleta(ruta: string): string {
    if (!ruta) return '';
    if (ruta.startsWith('http://') || ruta.startsWith('https://')) return ruta;
    const hostBase = environment.apiUrl.replace(/\/api\/?$/, '');
    const rutaLimpia = ruta.startsWith('/') ? ruta : `/${ruta}`;
    return `${hostBase}${rutaLimpia}`;
  }
}