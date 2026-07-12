import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Proveedor } from '../models/proveedor.model';

@Injectable({
  providedIn: 'root'
})
export class ProveedorService {
  private apiUrl = `${environment.apiUrl}/proveedores`;

  constructor(private http: HttpClient) { }

  /**
   * Obtiene la lista completa de proveedores ordenados por razón social.
   */
  listar(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene un proveedor por su ID.
   */
  obtener(id: number): Observable<Proveedor> {
    return this.http.get<Proveedor>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Crea un nuevo proveedor.
   */
  crear(data: Omit<Proveedor, 'id'>): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.apiUrl, data)
      .pipe(catchError(this.handleError));
  }

  /**
   * Actualiza un proveedor existente.
   */
  actualizar(id: number, data: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.apiUrl}/${id}`, data)
      .pipe(catchError(this.handleError));
  }

  /**
   * Elimina un proveedor por su ID.
   */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Busca proveedores por razón social (contiene el texto).
   */
  buscarPorRazonSocial(razonSocial: string): Observable<Proveedor[]> {
    const params = new HttpParams().set('razonSocial', razonSocial);
    return this.http.get<Proveedor[]>(`${this.apiUrl}/buscar`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene un proveedor por su RUC.
   */
  buscarPorRuc(ruc: string): Observable<Proveedor> {
    return this.http.get<Proveedor>(`${this.apiUrl}/ruc/${ruc}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Busca proveedores por región.
   */
  buscarPorRegion(region: string): Observable<Proveedor[]> {
    const params = new HttpParams().set('region', region);
    return this.http.get<Proveedor[]>(`${this.apiUrl}/region`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene la lista de regiones distintas.
   */
  obtenerRegiones(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/regiones`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Busca proveedores por nombre de contacto.
   */
  buscarPorContacto(contacto: string): Observable<Proveedor[]> {
    const params = new HttpParams().set('contacto', contacto);
    return this.http.get<Proveedor[]>(`${this.apiUrl}/buscar/contacto`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Verifica si ya existe un proveedor con el RUC indicado.
   */
  existePorRuc(ruc: string): Observable<boolean> {
    const params = new HttpParams().set('ruc', ruc);
    return this.http.get<boolean>(`${this.apiUrl}/existe`, { params })
      .pipe(catchError(this.handleError));
  }

  // ============================================================
  // MANEJO CENTRALIZADO DE ERRORES
  // ============================================================

  private handleError(error: any): Observable<never> {
    let errorMessage = 'Ocurrió un error en la petición.';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error de red: ${error.error.message}`;
      console.error('Error de red:', error.error.message);
    } else {
      errorMessage = error.error?.mensaje || error.message || errorMessage;
      console.error(`Error HTTP ${error.status}:`, error.error || error.message);
    }
    return throwError(() => new Error(errorMessage));
  }
}