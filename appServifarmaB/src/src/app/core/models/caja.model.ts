/**
 * Modelo que representa una caja registradora.
 */
export interface Caja {
  id?: number;

  // Fecha y hora en que se realizó la apertura de la caja.
  fechaApertura: string;

  // Fecha y hora del cierre de la caja (si existe).
  fechaCierre?: string;

  // Monto inicial con el que se abrió la caja.
  montoApertura: number;

  // Monto declarado por el usuario al cerrar la caja.
  montoCierreDeclarado?: number;

  // Identificador del usuario que realizó la apertura.
  usuarioAperturaId: number;

  // Identificador del usuario que realizó el cierre.
  usuarioCierreId?: number;

  // Estado actual de la caja.
  estado: 'abierta' | 'cerrada';

  // Nombre del usuario que abrió la caja.
  usuarioAperturaNombre?: string;

  // Nombre del usuario que cerró la caja.
  usuarioCierreNombre?: string;

  // Fecha de creación del registro.
  createdAt?: string;
}

/**
 * Datos necesarios para abrir una caja.
 */
export interface AperturaCajaRequest {
  usuarioAperturaId: number;
  montoApertura: number;
}

/**
 * Datos necesarios para cerrar una caja.
 */
export interface CierreCajaRequest {
  usuarioCierreId: number;
  montoCierreDeclarado: number;
}

/**
 * Respuesta generada al cerrar una caja.
 */
export interface CierreCajaResponse {
  cajaId: number;
  fechaApertura: string;
  fechaCierre: string;
  montoApertura: number;
  totalVentas: number;
  montoDeclarado: number;
  diferencia: number;
  usuarioApertura: string;
  usuarioCierre: string;
}