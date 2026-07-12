export interface RentabilidadCategoria {
  categoriaNombre: string;
  ingresos: number;
  costos: number;
  margen: number;
}

export interface ReporteRentabilidad {
  fechaInicio: string;
  fechaFin: string;
  ingresosTotales: number;
  costoVentas: number;
  mermas: number;
  margenBruto: number;
  margenNeto: number;
  categorias: RentabilidadCategoria[];
}