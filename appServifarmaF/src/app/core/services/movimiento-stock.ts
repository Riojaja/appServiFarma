import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MovimientoStock } from '../models/movimiento-stock.model';

@Injectable({
  providedIn: 'root'
})
export class MovimientoStockService {
  private apiUrl = `${environment.apiUrl}/movimientos-stock`;

  constructor(private http: HttpClient) { }

  listar(): Observable<MovimientoStock[]> {
    return this.http.get<MovimientoStock[]>(this.apiUrl);
  }

  listarPorLote(loteId: number): Observable<MovimientoStock[]> {
    return this.http.get<MovimientoStock[]>(`${this.apiUrl}/lote/${loteId}`);
  }

  listarPorUsuario(usuarioId: number): Observable<MovimientoStock[]> {
    return this.http.get<MovimientoStock[]>(`${this.apiUrl}/usuario/${usuarioId}`);
  }

  listarPorTipo(tipo: string): Observable<MovimientoStock[]> {
    return this.http.get<MovimientoStock[]>(`${this.apiUrl}/tipo/${tipo}`);
  }

  listarPorFecha(inicio: string, fin: string): Observable<MovimientoStock[]> {
    return this.http.get<MovimientoStock[]>(`${this.apiUrl}/fechas?inicio=${inicio}&fin=${fin}`);
  }

  listarPorLoteYTipo(loteId: number, tipo: string): Observable<MovimientoStock[]> {
    return this.http.get<MovimientoStock[]>(`${this.apiUrl}/lote/${loteId}/tipo?tipo=${tipo}`);
  }
}