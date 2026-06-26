import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Proveedor } from '../models/proveedor.model';

@Injectable({
  providedIn: 'root'
})
export class ProveedorService {
  private apiUrl = `${environment.apiUrl}/proveedores`;

  constructor(private http: HttpClient) { }

  listar(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(this.apiUrl);
  }

  obtener(id: number): Observable<Proveedor> {
    return this.http.get<Proveedor>(`${this.apiUrl}/${id}`);
  }

  crear(data: Proveedor): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.apiUrl, data);
  }

  actualizar(id: number, data: Proveedor): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.apiUrl}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorRazonSocial(razonSocial: string): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(`${this.apiUrl}/buscar?razonSocial=${razonSocial}`);
  }

  buscarPorRuc(ruc: string): Observable<Proveedor> {
    return this.http.get<Proveedor>(`${this.apiUrl}/ruc/${ruc}`);
  }

  buscarPorRegion(region: string): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(`${this.apiUrl}/region?region=${region}`);
  }

  obtenerRegiones(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/regiones`);
  }
}