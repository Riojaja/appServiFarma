export interface MovimientoStock {
  id?: number;
  loteId: number;
  usuarioId: number;
  tipoMovimiento: 'compra' | 'venta' | 'ajuste' | 'merma';
  cantidad: number;
  costoUnitario?: number;
  fecha: string;
  observacion?: string;
  referenciaId?: number;
  // Datos de relaciones (CORREGIDOS)
  nombreProducto?: string;   // ← Cambiar de 'productoNombre' a 'nombreProducto'
  nombreUsuario?: string;    // ← Cambiar de 'usuarioNombre' a 'nombreUsuario'
  createdAt?: string;
}