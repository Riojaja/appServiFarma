export interface DemandaInsatisfecha {
  id?: number;
  productoSolicitado: string;
  fecha: string;
  clienteDocumento?: string;
  usuarioId: number;
  // Datos de relaciones
  usuarioNombre?: string;
  createdAt?: string;
}

export interface DemandaInsatisfechaRequest {
  productoSolicitado: string;
  clienteDocumento?: string;
  usuarioId: number;
}