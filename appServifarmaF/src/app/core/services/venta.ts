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

  // Registrar venta
  registrar(request: VentaRequest): Observable<Venta> {
    return this.http.post<Venta>(this.apiUrl, request);
  }

  // Anular venta
  anular(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/anular`, {});
  }

  // Obtener venta por ID
  obtener(id: number): Observable<Venta> {
    return this.http.get<Venta>(`${this.apiUrl}/${id}`);
  }

  // Listar todas las ventas, o filtrar por usuario si se pasa el parámetro
  listar(usuarioId?: number): Observable<Venta[]> {
    if (usuarioId) {
      return this.http.get<Venta[]>(`${this.apiUrl}/usuario/${usuarioId}`);
    }
    return this.http.get<Venta[]>(this.apiUrl);
  }

  // Listar por cliente
  listarPorCliente(clienteId: number): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/cliente/${clienteId}`);
  }

  // Listar por fechas
  listarPorFecha(inicio: string, fin: string): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/fechas?inicio=${inicio}&fin=${fin}`);
  }

  // Listar por medio de pago
  listarPorMedioPago(medioPago: string): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/medio-pago/${medioPago}`);
  }

  // Listar por estado
  listarPorEstado(estado: string): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/estado/${estado}`);
  }

  // Últimas ventas
  obtenerUltimas(limite: number): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/ultimas?limite=${limite}`);
  }

  // Total de ventas en período
  obtenerTotalPeriodo(inicio: string, fin: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/total-periodo?inicio=${inicio}&fin=${fin}`);
  }

  enviarBoletaCorreo(ventaId: number, correoDestino: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${ventaId}/enviar-boleta`, { destino: correoDestino });
  }
}