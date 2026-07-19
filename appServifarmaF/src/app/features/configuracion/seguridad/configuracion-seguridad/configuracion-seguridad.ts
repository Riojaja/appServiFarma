import { Component, OnInit, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfiguracionService } from '../../../../core/services/configuracion';
import { ConfiguracionSeguridad } from '../../../../core/models/configuracion.model';
import { AuthService } from '../../../../core/auth';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-configuracion-seguridad',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './configuracion-seguridad.html',
  styleUrls: ['./configuracion-seguridad.css']
})
export class ConfiguracionSeguridadComponent implements OnInit, AfterViewInit {
  config: ConfiguracionSeguridad = {
    tiempo_inactividad_minutos: '',
    horas_cierre_turno: '',
    intentos_fallidos_maximos: '',
    bloqueo_minutos: ''
  };

  cargando: boolean = false;
  guardando: boolean = false;
  errorCarga: boolean = false;
  mensajeError: string = '';
  private primeraCarga: boolean = true; // para evitar duplicados

  get operacionEnCurso(): boolean {
    return this.cargando || this.guardando;
  }

  private valoresAnteriores: ConfiguracionSeguridad = { ...this.config };

  constructor(
    private configService: ConfiguracionService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    console.log('🔄 ngOnInit ejecutado');
    // La primera carga se hará en ngAfterViewInit para asegurar que la vista esté lista
  }

  ngAfterViewInit(): void {
    console.log('🔄 ngAfterViewInit ejecutado');
    // Si es la primera vez que se carga el componente, ejecutar la carga
    if (this.primeraCarga) {
      this.primeraCarga = false;
      // Pequeño retraso para asegurar que el DOM esté listo
      setTimeout(() => {
        this.cargarConfiguracion();
      }, 100);
    }
  }

  cargarConfiguracion(): void {
    if (this.operacionEnCurso) return;

    console.log('🔄 Cargando configuración...');
    this.cargando = true;
    this.errorCarga = false;
    this.mensajeError = '';

    this.configService.obtenerConfiguracion().subscribe({
      next: (data) => {
        console.log('✅ Configuración recibida:', data);
        this.config = { ...data };
        this.valoresAnteriores = { ...data };
        this.cargando = false;
        this.errorCarga = false;
        this.cdr.detectChanges(); // 🔥 Forzar actualización de la vista
      },
      error: (err) => {
        console.error('❌ Error al cargar configuración:', err);
        this.cargando = false;
        this.errorCarga = true;

        if (err.status === 403) {
          this.mensajeError = 'No tienes permisos para acceder a esta configuración. Contacta al administrador.';
        } else if (err.status === 401) {
          this.mensajeError = 'Tu sesión ha expirado. Inicia sesión nuevamente.';
          this.authService.logout();
          this.router.navigate(['/login']);
        } else {
          this.mensajeError = err.error?.mensaje || 'Error al cargar la configuración. Intenta de nuevo.';
        }

        this.cdr.detectChanges(); // 🔥 Forzar actualización de la vista

        Swal.fire({
          icon: 'error',
          title: 'Error al cargar',
          text: this.mensajeError,
          confirmButtonColor: '#2563eb',
          customClass: { popup: 'swal-farmaceutico' }
        });
      }
    });
  }

  guardarConfiguracion(): void {
    if (this.guardando) return;

    const errorValidacion = this.validarConfiguracion();
    if (errorValidacion) {
      Swal.fire({
        icon: 'warning',
        title: 'Datos inválidos',
        text: errorValidacion,
        confirmButtonColor: '#2563eb',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    const cambioTiempoInactividad = this.config.tiempo_inactividad_minutos !== this.valoresAnteriores.tiempo_inactividad_minutos;
    const cambioHorasCierre = this.config.horas_cierre_turno !== this.valoresAnteriores.horas_cierre_turno;

    const mensajeConfirmacion = cambioTiempoInactividad || cambioHorasCierre
      ? 'Se ha modificado el tiempo de inactividad o las horas de cierre de turno. Para aplicar estos cambios, se cerrará tu sesión actual y deberás iniciar sesión nuevamente. ¿Deseas continuar?'
      : '¿Estás seguro de guardar los cambios en la configuración de seguridad?';

    Swal.fire({
      title: 'Confirmar cambios',
      text: mensajeConfirmacion,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#2563eb',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, guardar',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.guardando = true;
      this.configService.actualizarConfiguracion(this.config).subscribe({
        next: () => {
          this.guardando = false;
          this.valoresAnteriores = { ...this.config };
          this.cdr.detectChanges();

          Swal.fire({
            icon: 'success',
            title: 'Configuración actualizada',
            text: 'Los cambios se aplicarán a partir de ahora.',
            timer: 2000,
            showConfirmButton: false,
            customClass: { popup: 'swal-farmaceutico' }
          });

          if (cambioTiempoInactividad || cambioHorasCierre) {
            Swal.fire({
              icon: 'info',
              title: 'Cerrando sesión',
              text: 'La sesión se cerrará para aplicar los nuevos parámetros de seguridad.',
              timer: 2500,
              showConfirmButton: false,
              customClass: { popup: 'swal-farmaceutico' }
            }).then(() => {
              this.authService.logout();
              this.router.navigate(['/login']);
            });
          }
        },
        error: (err) => {
          this.guardando = false;
          const mensaje = err?.error?.mensaje || err?.error?.message || 'No se pudo actualizar la configuración.';
          Swal.fire({
            icon: 'error',
            title: 'Error al guardar',
            text: mensaje,
            confirmButtonColor: '#2563eb',
            customClass: { popup: 'swal-farmaceutico' }
          });
        }
      });
    });
  }

  private validarConfiguracion(): string | null {
    const minutosInactividad = parseInt(this.config.tiempo_inactividad_minutos, 10);
    if (isNaN(minutosInactividad) || minutosInactividad < 1) {
      return 'El tiempo de inactividad debe ser un número entero mayor a 0 (ej. 5).';
    }

    // Validar horas de cierre (formato HH:MM o HH:MM,HH:MM)
    const horasArray = this.config.horas_cierre_turno.split(',').map(h => h.trim());
    if (horasArray.length === 0 || horasArray.some(h => !this.esHoraValida(h))) {
      return 'Formato inválido. Usa horas en formato HH:MM separadas por coma (ej. 12:30,18:45)';
    }

    const intentos = parseInt(this.config.intentos_fallidos_maximos, 10);
    if (isNaN(intentos) || intentos < 1) {
      return 'El número máximo de intentos fallidos debe ser un entero mayor a 0.';
    }

    const bloqueo = parseInt(this.config.bloqueo_minutos, 10);
    if (isNaN(bloqueo) || bloqueo < 1) {
      return 'Los minutos de bloqueo deben ser un entero mayor a 0.';
    }

    return null;
  }

  private esHoraValida(hora: string): boolean {
    const partes = hora.split(':');
    if (partes.length !== 2) return false;
    const h = parseInt(partes[0], 10);
    const m = parseInt(partes[1], 10);
    return !isNaN(h) && !isNaN(m) && h >= 0 && h < 24 && m >= 0 && m < 60;
  }

  reiniciarFormulario(): void {
    if (this.operacionEnCurso) return;
    Swal.fire({
      title: '¿Reiniciar cambios?',
      text: 'Se perderán todas las modificaciones sin guardar.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#6b7280',
      cancelButtonColor: '#2563eb',
      confirmButtonText: 'Reiniciar',
      cancelButtonText: 'Cancelar',
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (result.isConfirmed) {
        this.config = { ...this.valoresAnteriores };
        this.errorCarga = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'info',
          title: 'Formulario reiniciado',
          text: 'Los cambios sin guardar han sido descartados.',
          timer: 1500,
          showConfirmButton: false,
          customClass: { popup: 'swal-farmaceutico' }
        });
      }
    });
  }
}