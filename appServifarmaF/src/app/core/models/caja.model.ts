export interface Caja {
  id?: number;
  fechaApertura: string;
  fechaCierre?: string;
  montoApertura: number;
  montoCierreDeclarado?: number;
  usuarioAperturaId: number;
  usuarioCierreId?: number;
  estado: 'abierta' | 'cerrada';
  usuarioAperturaNombre?: string;
  usuarioCierreNombre?: string;
  createdAt?: string;
}

export interface AperturaCajaRequest {
  usuarioAperturaId: number;
  montoApertura: number;
}

export interface CierreCajaRequest {
  usuarioCierreId: number;
  montoCierreDeclarado: number;
}

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