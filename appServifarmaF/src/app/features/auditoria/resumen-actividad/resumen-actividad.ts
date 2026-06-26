import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuditoriaService } from '../../../core/services/auditoria';

@Component({
  selector: 'app-auditoria-resumen-actividad',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './resumen-actividad.html',
  styleUrls: ['./resumen-actividad.css']
})
export class ResumenActividadComponent implements OnInit {
  resumen: any = {};
  filtros = { fechaInicio: '', fechaFin: '' };
  cargando: boolean = false;

  constructor(private auditoriaService: AuditoriaService) { }

  ngOnInit(): void {
    this.cargarResumen();
  }

  cargarResumen(): void {
    this.cargando = true;
    if (!this.filtros.fechaInicio || !this.filtros.fechaFin) {
      const hoy = new Date();
      const mesAtras = new Date(hoy);
      mesAtras.setMonth(mesAtras.getMonth() - 1);
      this.filtros.fechaInicio = mesAtras.toISOString().split('T')[0];
      this.filtros.fechaFin = hoy.toISOString().split('T')[0];
    }

    this.auditoriaService.obtenerResumenActividad(this.filtros.fechaInicio, this.filtros.fechaFin).subscribe({
      next: (data) => {
        this.resumen = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error:', err);
        this.cargando = false;
      }
    });
  }
}