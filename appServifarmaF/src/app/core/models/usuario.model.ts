export interface Usuario {
  id?: number;
  nombreCompleto: string;
  usuario: string;
  contrasena?: string;
  rol: string; // 'admin' o 'vendedor'
  rolId?: number;
  activo: boolean;
  createdAt?: string;
  updatedAt?: string;
}