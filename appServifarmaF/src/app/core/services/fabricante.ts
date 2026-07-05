import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Fabricante } from '../models/fabricante.model';

@Injectable({
  providedIn: 'root'
})
export class FabricanteService {
  private apiUrl = `${environment.apiUrl}/fabricantes`;

  constructor(private http: HttpClient) { }

  listar(): Observable<Fabricante[]> {
    return this.http.get<Fabricante[]>(this.apiUrl);
  }

  obtener(id: number): Observable<Fabricante> {
    return this.http.get<Fabricante>(`${this.apiUrl}/${id}`);
  }

  crear(data: Fabricante): Observable<Fabricante> {
    return this.http.post<Fabricante>(this.apiUrl, data);
  }

  actualizar(id: number, data: Fabricante): Observable<Fabricante> {
    return this.http.put<Fabricante>(`${this.apiUrl}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorNombre(nombre: string): Observable<Fabricante[]> {
    return this.http.get<Fabricante[]>(`${this.apiUrl}/buscar?nombre=${nombre}`);
  }

  /** Útil para validar en vivo si ya existe un fabricante con ese nombre antes de enviar el formulario. */
  existePorNombre(nombre: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/existe?nombre=${nombre}`);
  }
}