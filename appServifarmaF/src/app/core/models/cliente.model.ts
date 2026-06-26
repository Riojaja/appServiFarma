export interface Cliente {
  id?: number;
  nombre: string;
  documentoTipo: 'DNI' | 'RUC' | 'Pasaporte';
  documentoNumero: string;
  telefono?: string;
  direccion?: string;
  email?: string;
  createdAt?: string; 
}