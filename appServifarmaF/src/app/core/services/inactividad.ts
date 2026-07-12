// src/app/core/services/inactividad.service.ts
import { Injectable, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth';
import { ConfiguracionService } from './configuracion';
import Swal from 'sweetalert2';

@Injectable({ providedIn: 'root' })
export class InactividadService implements OnDestroy {
  private timer: any;
  private timeoutMinutos: number = 30;
  private eventos = ['click', 'keydown', 'scroll', 'mousemove', 'touchstart'];

  constructor(
    private authService: AuthService,
    private router: Router,
    private configService: ConfiguracionService
  ) {
    this.cargarConfiguracion();
    this.iniciarMonitoreo();
  }

  private cargarConfiguracion(): void {
    this.configService.obtenerConfiguracion().subscribe({
      next: (config) => {
        const minutos = parseInt(config.tiempo_inactividad_minutos, 10);
        if (!isNaN(minutos) && minutos > 0) {
          this.timeoutMinutos = minutos;
        }
      },
      error: () => {
        this.timeoutMinutos = 30; // fallback
      }
    });
  }

  private iniciarMonitoreo(): void {
    this.resetTimer();
    this.eventos.forEach(event => {
      document.addEventListener(event, () => this.resetTimer());
    });
  }

  private resetTimer(): void {
    clearTimeout(this.timer);
    if (!this.authService.isAuthenticated()) return;
    this.timer = setTimeout(() => {
      this.cerrarSesionPorInactividad();
    }, this.timeoutMinutos * 60 * 1000);
  }

  private cerrarSesionPorInactividad(): void {
    if (!this.authService.isAuthenticated()) return;
    Swal.fire({
      icon: 'warning',
      title: 'Sesión expirada',
      text: `Su sesión ha sido cerrada por inactividad (${this.timeoutMinutos} minutos).`,
      confirmButtonColor: '#0d9488',
      confirmButtonText: 'Aceptar'
    }).then(() => {
      this.authService.logout();
      this.router.navigate(['/login']);
    });
  }

  reiniciar(): void {
    this.resetTimer();
  }

  ngOnDestroy(): void {
    clearTimeout(this.timer);
    this.eventos.forEach(event => {
      document.removeEventListener(event, () => this.resetTimer());
    });
  }
}