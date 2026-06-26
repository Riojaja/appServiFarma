import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../../core/services/reporte';

@Component({
  selector: 'app-reporte-digemit',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './digemit.html',
  styleUrls: ['./digemit.css']
})
export class DigemitComponent {
  mes: string = '';
  formato: string = 'excel';
  cargando: boolean = false;
  errorMessage: string = '';

  constructor(private reporteService: ReporteService) { }

  generar(): void {
    if (!this.mes) {
      this.errorMessage = 'Seleccione un mes.';
      return;
    }

    this.cargando = true;
    this.errorMessage = '';
    this.reporteService.generarDigemit(this.mes, this.formato).subscribe({
      next: (blob) => {
        this.cargando = false;
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte_digemit_${this.mes}.${this.formato === 'excel' ? 'xlsx' : this.formato}`;
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