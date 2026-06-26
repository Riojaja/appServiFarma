import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DemandaInsatisfecha, DemandaInsatisfechaRequest } from '../models/demanda-insatisfecha.model';

@Injectable({
  providedIn: 'root'
})
export class DemandaService {
  private apiUrl = `${environment.apiUrl}/demandas-insatisfechas`;

  constructor(private http: HttpClient) { }

  // Registrar demanda
  registrar(request: DemandaInsatisfechaRequest): Observable<DemandaInsatisfecha> {
    return this.http.post<DemandaInsatisfecha>(this.apiUrl, request);
  }

  // Obtener por ID
  obtener(id: number): Observable<DemandaInsatisfecha> {
    return this.http.get<DemandaInsatisfecha>(`${this.apiUrl}/${id}`);
  }

  // Listar todas
  listar(): Observable<DemandaInsatisfecha[]> {
    return this.http.get<DemandaInsatisfecha[]>(this.apiUrl);
  }

  // Listar por usuario
  listarPorUsuario(usuarioId: number): Observable<DemandaInsatisfecha[]> {
    return this.http.get<DemandaInsatisfecha[]>(`${this.apiUrl}/usuario/${usuarioId}`);
  }

  // Listar por producto (nombre parcial)
  listarPorProducto(producto: string): Observable<DemandaInsatisfecha[]> {
    return this.http.get<DemandaInsatisfecha[]>(`${this.apiUrl}/producto?productoSolicitado=${producto}`);
  }

  // Listar por fechas
  listarPorFecha(inicio: string, fin: string): Observable<DemandaInsatisfecha[]> {
    return this.http.get<DemandaInsatisfecha[]>(`${this.apiUrl}/fechas?inicio=${inicio}&fin=${fin}`);
  }

  // Listar por documento de cliente
  listarPorClienteDocumento(documento: string): Observable<DemandaInsatisfecha[]> {
    return this.http.get<DemandaInsatisfecha[]>(`${this.apiUrl}/cliente-documento?clienteDocumento=${documento}`);
  }

  // Contar demandas en período
  contarPeriodo(inicio: string, fin: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/contar-periodo?inicio=${inicio}&fin=${fin}`);
  }

  // Eliminar
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}