export interface Producto {
  id?: number;
  nombre: string;
  codigoBarras?: string;
  principioActivo?: string;
  imagen?: string;
  esGenerico: boolean;
  precioVentaActual: number;
  stockMinimo: number;
  // Relaciones
  categoriaId?: number;
  fabricanteId?: number;
  productoGenericoId?: number;
  // Datos de las relaciones (para mostrar en listados)
  categoriaNombre?: string;
  fabricanteNombre?: string;
  productoGenericoNombre?: string;
  // Stock actual (calculado en el frontend o desde el backend)
  stockActual?: number;  // <--- AGREGAR ESTA LÍNEA
  createdAt?: string;
  updatedAt?: string;

}