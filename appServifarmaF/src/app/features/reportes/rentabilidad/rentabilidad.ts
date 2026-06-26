import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../../core/services/reporte';

@Component({
  selector: 'app-reporte-rentabilidad',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './rentabilidad.html',
  styleUrls: ['./rentabilidad.css']
})
export class RentabilidadComponent {
  fechaInicio: string = '';
  fechaFin: string = '';
  formato: string = 'excel';
  cargando: boolean = false;
  errorMessage: string = '';

  constructor(private reporteService: ReporteService) { }

  generar(): void {
    if (!this.fechaInicio || !this.fechaFin) {
      this.errorMessage = 'Seleccione ambas fechas.';
      return;
    }

    this.cargando = true;
    this.errorMessage = '';
    this.reporteService.generarRentabilidad(this.fechaInicio, this.fechaFin, this.formato).subscribe({
      next: (blob) => {
        this.cargando = false;
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `rentabilidad_${this.fechaInicio}_${this.fechaFin}.${this.formato === 'excel' ? 'xlsx' : this.formato}`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.cargando = false;
        this.errorMessage = err.error?.mensaje || 'Error al generar el reporte.';
        console.error('Error:', err);
      }
    });
  }
}