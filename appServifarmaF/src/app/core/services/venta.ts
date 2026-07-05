import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Venta, VentaRequest } from '../models/venta.model';

@Injectable({
  providedIn: 'root'
})
export class VentaService {
  private apiUrl = `${environment.apiUrl}/ventas`;

  constructor(private http: HttpClient) { }

  registrar(request: VentaRequest): Observable<Venta> {
    return this.http.post<Venta>(this.apiUrl, request);
  }

  anular(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/anular`, {});
  }

  obtener(id: number): Observable<Venta> {
    return this.http.get<Venta>(`${this.apiUrl}/${id}`);
  }

  listar(usuarioId?: number): Observable<Venta[]> {
    if (usuarioId) {
      return this.http.get<Venta[]>(`${this.apiUrl}/usuario/${usuarioId}`);
    }
    return this.http.get<Venta[]>(this.apiUrl);
  }

  listarPorCliente(clienteId: number): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/cliente/${clienteId}`);
  }

  listarPorFecha(inicio: string, fin: string): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/fechas?inicio=${inicio}&fin=${fin}`);
  }

  listarPorMedioPago(medioPago: string): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/medio-pago/${medioPago}`);
  }

  listarPorEstado(estado: string): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/estado/${estado}`);
  }

  obtenerUltimas(limite: number): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/ultimas?limite=${limite}`);
  }

  obtenerTotalPeriodo(inicio: string, fin: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/total-periodo?inicio=${inicio}&fin=${fin}`);
  }

  /** Desglose del total de ventas por cada medio de pago en un rango de fechas. */
  obtenerTotalPorMedioPagoYPeriodo(inicio: string, fin: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/total-medio-pago-periodo?inicio=${inicio}&fin=${fin}`);
  }

  enviarBoletaCorreo(ventaId: number, correoDestino: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${ventaId}/enviar-boleta`, { destino: correoDestino });
  }
}