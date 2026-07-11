export interface Alerta {
  tipo: 'stock_bajo' | 'proximo_vencer' | 'vencido';
  mensaje: string;
  productoId: number;
  productoNombre: string;
  loteId?: number;
  cantidad?: number;
  fechaVencimiento?: string;
  diasRestantes?: number;
  categoriaNombre?: string;
}