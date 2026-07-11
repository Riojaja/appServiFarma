import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Categoria } from '../models/categoria.model';

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {
  private apiUrl = `${environment.apiUrl}/categorias`;

  constructor(private http: HttpClient) { }

  /**
   * Obtiene la lista completa de categorías ordenadas alfabéticamente.
   * @returns Observable con array de categorías.
   */
  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene una categoría por su ID.
   * @param id ID de la categoría.
   * @returns Observable con la categoría.
   */
  obtener(id: number): Observable<Categoria> {
    return this.http.get<Categoria>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Crea una nueva categoría.
   * @param data Datos de la categoría (sin ID).
   * @returns Observable con la categoría creada.
   */
  crear(data: Omit<Categoria, 'id'>): Observable<Categoria> {
    return this.http.post<Categoria>(this.apiUrl, data)
      .pipe(catchError(this.handleError));
  }

  /**
   * Actualiza una categoría existente.
   * @param id ID de la categoría a actualizar.
   * @param data Datos actualizados de la categoría.
   * @returns Observable con la categoría actualizada.
   */
  actualizar(id: number, data: Partial<Categoria>): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.apiUrl}/${id}`, data)
      .pipe(catchError(this.handleError));
  }

  /**
   * Elimina una categoría por su ID.
   * @param id ID de la categoría a eliminar.
   * @returns Observable vacío (void).
   */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Busca categorías cuyo nombre contenga el texto proporcionado.
   * @param nombre Texto parcial o completo del nombre.
   * @returns Observable con array de categorías que coinciden.
   */
  buscarPorNombre(nombre: string): Observable<Categoria[]> {
    const params = new HttpParams().set('nombre', nombre);
    return this.http.get<Categoria[]>(`${this.apiUrl}/buscar`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Verifica si ya existe una categoría con el nombre indicado (case-insensitive).
   * Útil para validaciones en tiempo real en formularios.
   * @param nombre Nombre a verificar.
   * @returns Observable<boolean> true si existe, false si no.
   */
  existePorNombre(nombre: string): Observable<boolean> {
    const params = new HttpParams().set('nombre', nombre);
    return this.http.get<boolean>(`${this.apiUrl}/existe`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtiene el total de categorías registradas.
   * Útil para mostrar contadores en el dashboard.
   * @returns Observable con el número total.
   */
  contar(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/contar`)
      .pipe(
        catchError(this.handleError)
      );
  }

  // ============================================================
  // MANEJO CENTRALIZADO DE ERRORES
  // ============================================================

  /**
   * Maneja errores HTTP de forma centralizada.
   * @param error Error capturado desde la petición.
   * @returns Observable con error lanzado.
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'Ocurrió un error en la petición.';
    
    if (error.error instanceof ErrorEvent) {
      // Error del lado del cliente (red, etc.)
      errorMessage = `Error de red: ${error.error.message}`;
      console.error('Error de red:', error.error.message);
    } else {
      // El backend devolvió un código de error HTTP
      errorMessage = error.error?.mensaje || error.message || errorMessage;
      console.error(`Error HTTP ${error.status}:`, error.error || error.message);
    }

    // Puedes usar un servicio de notificaciones aquí si lo tienes
    // this.notificationService.error(errorMessage);
    
    return throwError(() => new Error(errorMessage));
  }
}