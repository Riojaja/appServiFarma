import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Fabricante } from '../models/fabricante.model';

@Injectable({
  providedIn: 'root'
})
export class FabricanteService {
  private apiUrl = `${environment.apiUrl}/fabricantes`;

  constructor(private http: HttpClient) { }

  listar(): Observable<Fabricante[]> {
    return this.http.get<Fabricante[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  obtener(id: number): Observable<Fabricante> {
    return this.http.get<Fabricante>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  crear(data: Omit<Fabricante, 'id'>): Observable<Fabricante> {
    return this.http.post<Fabricante>(this.apiUrl, data)
      .pipe(catchError(this.handleError));
  }

  actualizar(id: number, data: Partial<Fabricante>): Observable<Fabricante> {
    return this.http.put<Fabricante>(`${this.apiUrl}/${id}`, data)
      .pipe(catchError(this.handleError));
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  buscarPorNombre(nombre: string): Observable<Fabricante[]> {
    const params = new HttpParams().set('nombre', nombre);
    return this.http.get<Fabricante[]>(`${this.apiUrl}/buscar`, { params })
      .pipe(catchError(this.handleError));
  }

  existePorNombre(nombre: string): Observable<boolean> {
    const params = new HttpParams().set('nombre', nombre);
    return this.http.get<boolean>(`${this.apiUrl}/existe`, { params })
      .pipe(catchError(this.handleError));
  }

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