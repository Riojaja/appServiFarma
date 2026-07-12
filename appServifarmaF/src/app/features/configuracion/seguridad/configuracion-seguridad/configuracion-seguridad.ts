// src/app/features/configuracion/seguridad/configuracion-seguridad/configuracion-seguridad.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfiguracionService } from '../../../../core/services/configuracion';
import { ConfiguracionSeguridad } from '../../../../core/models/configuracion.model';
import { AuthService } from '../../../../core/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-configuracion-seguridad',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './configuracion-seguridad.html',
  styleUrls: ['./configuracion-seguridad.css']
})
export class ConfiguracionSeguridadComponent implements OnInit {
  config: ConfiguracionSeguridad = {
    tiempo_inactividad_minutos: '',
    horas_cierre_turno: '',
    intentos_fallidos_maximos: '',
    bloqueo_minutos: ''
  };

  cargando: boolean = false;   // carga inicial (skeleton de la página)
  guardando: boolean = false;  // mientras se envía el guardado (evita doble-click)

  /** Bloquea todos los controles mientras hay cualquier operación en curso */
  get operacionEnCurso(): boolean {
    return this.cargando || this.guardando;
  }

  constructor(
    private configService: ConfiguracionService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.cargarConfiguracion();
  }

  cargarConfiguracion(): void {
    if (this.operacionEnCurso) { return; }
    this.cargando = true;
    this.configService.obtenerConfiguracion().subscribe({
      next: (data) => {
        this.config = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar configuración:', err);
        Swal.fire('Error', 'No se pudo cargar la configuración', 'error');
        this.cargando = false;
      }
    });
  }

  guardarConfiguracion(): void {
    if (this.guardando) { return; } // 🔒 evita doble envío por doble-click

    // ===== Validaciones =====
    const minutosInactividad = parseInt(this.config.tiempo_inactividad_minutos, 10);
    if (isNaN(minutosInactividad) || minutosInactividad < 1) {
      Swal.fire('Error', 'El tiempo de inactividad debe ser un número mayor a 0', 'error');
      return;
    }

    const horas = this.config.horas_cierre_turno.split(',').map((h: string) => parseInt(h.trim(), 10));
    if (horas.length === 0 || horas.some((h: number) => isNaN(h) || h < 0 || h > 23)) {
      Swal.fire('Error', 'Las horas de cierre deben ser números entre 0 y 23, separados por coma', 'error');
      return;
    }

    const intentos = parseInt(this.config.intentos_fallidos_maximos, 10);
    if (isNaN(intentos) || intentos < 1) {
      Swal.fire('Error', 'Los intentos fallidos máximos deben ser un número mayor a 0', 'error');
      return;
    }

    const bloqueo = parseInt(this.config.bloqueo_minutos, 10);
    if (isNaN(bloqueo) || bloqueo < 1) {
      Swal.fire('Error', 'Los minutos de bloqueo deben ser un número mayor a 0', 'error');
      return;
    }

    this.guardando = true;
    this.configService.actualizarConfiguracion(this.config).subscribe({
      next: () => {
        this.guardando = false;
        Swal.fire({
          icon: 'success',
          title: 'Configuración actualizada',
          text: 'Los cambios se aplicarán a los nuevos inicios de sesión.',
          confirmButtonColor: '#2563eb'
        });
      },
      error: (err) => {
        console.error('Error al actualizar:', err);
        this.guardando = false;
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: err?.error?.mensaje || err?.error?.message || 'No se pudo actualizar la configuración',
          confirmButtonColor: '#2563eb'
        });
      }
    });
  }
}