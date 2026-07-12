// src/app/core/models/configuracion.model.ts
export interface ConfiguracionSeguridad {
  tiempo_inactividad_minutos: string;
  horas_cierre_turno: string;
  intentos_fallidos_maximos: string;
  bloqueo_minutos: string;
}