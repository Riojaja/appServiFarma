import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
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

  listar(): Observable<Lote[]> {
    return this.http.get<Lote[]>(this.apiUrl);
  }

  obtener(id: number): Observable<Lote> {
    return this.http.get<Lote>(`${this.apiUrl}/${id}`);
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

  listarPorProducto(productoId: number): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${this.apiUrl}/producto/${productoId}`);
  }

  listarPorEstado(estado: string): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${this.apiUrl}/estado/${estado}`);
  }

  buscarPorLote(numero: string): Observable<Lote> {
    return this.http.get<Lote>(`${this.apiUrl}/numero/${numero}`);
  }

  obtenerProximosAVencer(dias: number): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${this.apiUrl}/proximos-a-vencer?diasAnticipacion=${dias}`);
  }

  obtenerVencidos(): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${this.apiUrl}/vencidos`);
  }

  marcarDeteriorado(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/deteriorado`, {});
  }

  /** Ajuste manual de stock: 'ajuste' aumenta, 'merma' reduce. La cantidad siempre va positiva. */
  ajustarStock(id: number, data: AjusteStockRequest): Observable<{ mensaje: string }> {
    return this.http.patch<{ mensaje: string }>(`${this.apiUrl}/${id}/stock`, data);
  }
}