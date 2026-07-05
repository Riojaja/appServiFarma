import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cliente } from '../models/cliente.model';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private apiUrl = `${environment.apiUrl}/clientes`;

  constructor(private http: HttpClient) { }

  listar(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.apiUrl);
  }

  obtener(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.apiUrl}/${id}`);
  }

  crear(data: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, data);
  }

  actualizar(id: number, data: Cliente): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.apiUrl}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorNombre(nombre: string): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.apiUrl}/buscar?nombre=${nombre}`);
  }

  buscarPorDocumento(documento: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.apiUrl}/documento/${documento}`);
  }

  buscarPorTipoDocumento(tipo: string): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.apiUrl}/tipo?tipo=${tipo}`);
  }

  /** Útil para validar en vivo si ya existe un cliente con ese documento antes de enviar el formulario. */
  existePorDocumento(documento: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/existe?documento=${documento}`);
  }
}