import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Producto } from '../models/producto.model';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private apiUrl = `${environment.apiUrl}/productos`;

  constructor(private http: HttpClient) { }

  // ==================== CRUD ====================
  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl).pipe(
      map(productos => productos.map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      })))
    );
  }

  obtener(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`).pipe(
      map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      }))
    );
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

  // ==================== BÚSQUEDAS ====================
  buscarPorNombre(nombre: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/buscar?nombre=${nombre}`).pipe(
      map(productos => productos.map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      })))
    );
  }

  buscarPorCodigoBarras(codigo: string): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/codigo-barras/${codigo}`).pipe(
      map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      }))
    );
  }

  buscarPorPrincipioActivo(principio: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/buscar/principio-activo?principioActivo=${principio}`).pipe(
      map(productos => productos.map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      })))
    );
  }

  buscarPorNombreOCodigo(texto: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/buscar/texto?texto=${texto}`).pipe(
      map(productos => productos.map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      })))
    );
  }

  listarPorCategoria(categoriaId: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/categoria/${categoriaId}`).pipe(
      map(productos => productos.map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      })))
    );
  }

  listarPorFabricante(fabricanteId: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/fabricante/${fabricanteId}`).pipe(
      map(productos => productos.map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      })))
    );
  }

  listarGenericos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/genericos`).pipe(
      map(productos => productos.map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      })))
    );
  }

  obtenerStock(id: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${id}/stock`);
  }

  obtenerAlternativas(id: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/${id}/alternativas`).pipe(
      map(productos => productos.map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      })))
    );
  }

  obtenerProductosConStockBajo(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/alertas/stock-bajo`).pipe(
      map(productos => productos.map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      })))
    );
  }

  obtenerProductosSinStock(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/alertas/sin-stock`).pipe(
      map(productos => productos.map(p => ({
        ...p,
        imagen: p.imagen ? this.obtenerUrlCompleta(p.imagen) : undefined
      })))
    );
  }

  existePorCodigoBarras(codigo: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/existe?codigo=${codigo}`);
  }

  // ==================== IMPORTACIÓN ====================
  descargarPlantilla(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/importacion/plantilla`, {
      responseType: 'blob'
    });
  }

  importarProductos(archivo: File): Observable<any> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post(`${this.apiUrl}/importacion/subir`, formData);
  }

  // ==================== IMÁGENES ====================
  subirImagen(id: number, imagen: File): Observable<any> {
    const formData = new FormData();
    formData.append('imagen', imagen);
    return this.http.post(`${this.apiUrl}/${id}/imagen`, formData);
  }

  actualizarImagenDesdeUrl(id: number, url: string): Observable<any> {
    const params = new HttpParams().set('url', url);
    return this.http.post(`${this.apiUrl}/${id}/imagen-url`, null, { params });
  }

  // ==================== UTILIDAD ====================
  private obtenerUrlCompleta(ruta: string): string {
    if (!ruta) return '';
    if (ruta.startsWith('http://') || ruta.startsWith('https://')) {
      return ruta;
    }

    // Las imágenes/archivos estáticos casi siempre se sirven desde la raíz del
    // servidor (ej. http://localhost:8080/uploads/xyz.jpg), NO bajo el prefijo
    // de la API (ej. http://localhost:8080/api). Si usáramos environment.apiUrl
    // directo, la URL quedaría con el /api de más y el navegador recibiría 404.
    const hostBase = environment.apiUrl.replace(/\/api\/?$/, '');
    const rutaLimpia = ruta.startsWith('/') ? ruta : `/${ruta}`;
    return `${hostBase}${rutaLimpia}`;
  }
}