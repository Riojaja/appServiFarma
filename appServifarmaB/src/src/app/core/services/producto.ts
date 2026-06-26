import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Producto } from '../models/producto.model';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private apiUrl = `${environment.apiUrl}/productos`;

  constructor(private http: HttpClient) { }

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl);
  }

  obtener(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`);
  }

  crear(data: Producto): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, data);
  }

  actualizar(id: number, data: Producto): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorNombre(nombre: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/buscar?nombre=${nombre}`);
  }

  buscarPorCodigoBarras(codigo: string): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/codigo-barras/${codigo}`);
  }

  buscarPorPrincipioActivo(principio: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/buscar/principio-activo?principioActivo=${principio}`);
  }

  buscarPorNombreOCodigo(texto: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/buscar/texto?texto=${texto}`);
  }

  listarPorCategoria(categoriaId: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/categoria/${categoriaId}`);
  }

  listarPorFabricante(fabricanteId: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/fabricante/${fabricanteId}`);
  }

  listarGenericos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/genericos`);
  }

  obtenerStock(id: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${id}/stock`);
  }

  obtenerAlternativas(id: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/${id}/alternativas`);
  }


  obtenerProductosConStockBajo(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/alertas/stock-bajo`);
  }

  obtenerProductosSinStock(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/alertas/sin-stock`);
  }


  existePorCodigoBarras(codigo: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/existe?codigo=${codigo}`);
  }
}