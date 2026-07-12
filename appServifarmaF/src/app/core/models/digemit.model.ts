export interface DigemitItem {
  codigoProducto: string;
  nombreProducto: string;
  laboratorio: string;
  principioActivo: string;
  lote: string;
  fechaVencimiento: string;
  cantidad: number;
}

export interface ReporteDigemit {
  mes: string;
  fechaGeneracion: string;
  items: DigemitItem[];
}