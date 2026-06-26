import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Caja, AperturaCajaRequest, CierreCajaRequest, CierreCajaResponse } from '../models/caja.model';

@Injectable({
  providedIn: 'root'
})
export class CajaService {
  private apiUrl = `${environment.apiUrl}/caja`;

  constructor(private http: HttpClient) { }

  abrir(request: AperturaCajaRequest): Observable<Caja> {
    return this.http.post<Caja>(`${this.apiUrl}/apertura`, request);
  }

  cerrar(request: CierreCajaRequest): Observable<CierreCajaResponse> {
    return this.http.post<CierreCajaResponse>(`${this.apiUrl}/cierre`, request);
  }

  obtenerCajaAbierta(): Observable<Caja> {
    return this.http.get<Caja>(`${this.apiUrl}/abierta`);
  }

  obtenerPorId(id: number): Observable<Caja> {
    return this.http.get<Caja>(`${this.apiUrl}/${id}`);
  }

  listar(): Observable<Caja[]> {
    return this.http.get<Caja[]>(this.apiUrl);
  }

  existeCajaAbierta(): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/existe-abierta`);
  }

  usuarioTieneCajaAbierta(usuarioId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/usuario/${usuarioId}/tiene-abierta`);
  }

  obtenerTotalVentas(cajaId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${cajaId}/total-ventas`);
  }

  obtenerTotalVentasPorMedioPago(cajaId: number, medioPago: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${cajaId}/total-ventas/${medioPago}`);
  }

}