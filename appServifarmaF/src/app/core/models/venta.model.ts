export interface Venta {
  id?: number;
  fecha: string;
  usuarioId: number;
  clienteId?: number;
  total: number;
  medioPago: 'efectivo' | 'tarjeta' | 'transferencia' | 'yape';
  codigoAutorizacion?: string;
  cajaId: number;
  estado: 'completada' | 'anulada';
  usuarioNombre?: string;
  clienteNombre?: string;
  detalles?: DetalleVenta[];
  createdAt?: string;
}

export interface DetalleVenta {
  id?: number;
  ventaId: number;
  loteId: number;        // <--- Usar este para identificar
  cantidad: number;
  precioUnitarioVenta: number;
  precioCompraUnitario: number;
  subtotal: number;
  productoNombre?: string;
  loteNumero?: string;
  createdAt?: string;
}
export interface VentaRequest {
  usuarioId: number;
  clienteId?: number;
  medioPago: 'efectivo' | 'tarjeta' | 'transferencia' | 'yape';
  codigoAutorizacion?: string;
  detalles: DetalleVentaRequest[];
  cajaId: number;
}

export interface DetalleVentaRequest {
  productoId: number;
  cantidad: number;
}