// src/app/core/models/configuracion.model.ts
export interface ConfiguracionSeguridad {
  tiempo_inactividad_minutos: string;
  horas_cierre_turno: string;  // acepta "15:30" o "15:30,18:45"
  intentos_fallidos_maximos: string;
  bloqueo_minutos: string;
}