export interface BitacoraComunicacion {
  id?: number;
  usuarioId: number;
  fechaHora: string;
  mensaje: string;
  tipo: 'novedad' | 'recordatorio' | 'incidencia';
  leido: boolean;
  // Datos de relaciones
  usuarioNombre?: string;
  createdAt?: string;
}

export interface BitacoraComunicacionRequest {
  usuarioId: number;
  mensaje: string;
  tipo: 'novedad' | 'recordatorio' | 'incidencia';
}