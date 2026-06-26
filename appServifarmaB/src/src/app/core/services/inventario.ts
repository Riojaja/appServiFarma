import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Alerta } from '../models/alerta.model';

@Injectable({
  providedIn: 'root'
})
export class InventarioService {
  private apiUrl = `${environment.apiUrl}/inventario`;

  constructor(private http: HttpClient) { }

  // Alertas de stock bajo
  obtenerStockBajo(): Observable<Alerta[]> {
    return this.http.get<Alerta[]>(`${this.apiUrl}/stock-bajo`);
  }

  // Alertas de productos próximos a vencer
  obtenerProximosVencer(): Observable<Alerta[]> {
    return this.http.get<Alerta[]>(`${this.apiUrl}/proximos-vencer`);
  }

  // Resumen de inventario (productos con stock, sin stock, etc.)
  obtenerResumen(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/resumen`);
  }

  // Productos sin stock
  obtenerSinStock(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/sin-stock`);
  }
}