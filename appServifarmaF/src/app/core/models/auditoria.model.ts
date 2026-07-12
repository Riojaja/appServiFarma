// ============================================================
// MODELOS PARA AUDITORÍA
// ============================================================

/** Movimiento de stock (respuesta del backend) */
export interface MovimientoStock {
  id: number;
  tipoMovimiento: 'compra' | 'venta' | 'ajuste' | 'merma';
  cantidad: number;
  costoUnitario: number;
  fecha: string; // LocalDateTime en formato ISO
  usuarioId: number;
  loteId: number;
  usuarioNombre?: string;
  loteCodigo?: string;
  observacion?: string;
}

/** Venta para auditoría (respuesta del backend) */
export interface VentaAuditoria {
  id: number;
  total: number;
  fecha: string;
  estado: 'completada' | 'anulada';
  usuarioId: number;
  usuarioNombre?: string;
  cajaId?: number;
  clienteNombre?: string;
}

/** Caja con diferencia (respuesta del backend) */
export interface CajaDiferencia {
  cajaId: number;
  fechaApertura: string;
  fechaCierre: string;
  totalVentas: number;
  montoDeclarado: number;
  diferencia: number;
  tipo: 'SOBRANTE' | 'FALTANTE';
}

/** Historial de caja por usuario (respuesta del backend) */
export interface HistorialCaja {
  cajaId: number;
  fechaApertura: string;
  fechaCierre: string | null;
  montoApertura: number;
  montoCierreDeclarado: number | null;
  estado: 'abierta' | 'cerrada';
  totalVentas?: number;
  diferencia?: number;
}

/** Resumen de actividad (respuesta del backend) */
export interface ResumenActividad {
  fechaInicio: string;
  fechaFin: string;
  totalVentas: number;
  totalTransacciones: number;
  totalAnuladas: number;
  totalMovimientosStock: number;
  movimientosPorTipo: {
    [key: string]: number; // ej: { "compra": 5, "venta": 10, ... }
  };
  totalCajasCerradas: number;
}

/** Usuario básico para filtros */
export interface UsuarioBasico {
  id: number;
  nombre: string;
}