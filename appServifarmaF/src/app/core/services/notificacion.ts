import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type TipoNotificacion = 'exito' | 'error' | 'advertencia' | 'info';

export interface Notificacion {
  id: number;
  tipo: TipoNotificacion;
  mensaje: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificacionService {
  private contador = 0;
  private readonly notificacionesSubject = new BehaviorSubject<Notificacion[]>([]);
  readonly notificaciones$ = this.notificacionesSubject.asObservable();

  private mostrar(tipo: TipoNotificacion, mensaje: string, duracionMs = 4000): void {
    const id = ++this.contador;
    const actuales = this.notificacionesSubject.value;
    this.notificacionesSubject.next([...actuales, { id, tipo, mensaje }]);

    setTimeout(() => this.cerrar(id), duracionMs);
  }

  exito(mensaje: string): void {
    this.mostrar('exito', mensaje);
  }

  error(mensaje: string): void {
    this.mostrar('error', mensaje, 6000);
  }

  advertencia(mensaje: string): void {
    this.mostrar('advertencia', mensaje, 5000);
  }

  info(mensaje: string): void {
    this.mostrar('info', mensaje);
  }

  cerrar(id: number): void {
    const actuales = this.notificacionesSubject.value.filter(n => n.id !== id);
    this.notificacionesSubject.next(actuales);
  }
}