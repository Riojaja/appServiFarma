export interface Usuario {
  id: number;
  nombreCompleto: string;
  usuario: string;
  rol: { id: number; nombre: string };
  activo: boolean;
  intentosFallidos?: number;
  bloqueadoHasta?: string;
  ultimoAcceso?: string;
}