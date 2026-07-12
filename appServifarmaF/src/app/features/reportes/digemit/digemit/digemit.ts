import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../../../core/services/reporte';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-digemit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './digemit.html',
  styleUrls: ['./digemit.css']
})
export class DigemitComponent {
  mesDigemit: string = '';
  cargando: boolean = false;

  constructor(private reporteService: ReporteService) {
    const hoy = new Date();
    this.mesDigemit = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}`;
  }

  exportar(formato: string): void {
    if (!this.mesDigemit) {
      Swal.fire('Error', 'Selecciona un mes', 'error');
      return;
    }
    this.cargando = true;
    this.reporteService.generarDigemit(this.mesDigemit, formato).subscribe({
      next: (blob) => {
        const ext = formato === 'pdf' ? 'pdf' : (formato === 'csv' ? 'csv' : 'xlsx');
        this.descargarArchivo(blob, `digemit_${this.mesDigemit}.${ext}`);
        Swal.fire('Éxito', 'Reporte DIGEMID exportado correctamente', 'success');
      },
      error: (err) => {
        console.error('Error:', err);
        Swal.fire('Error', 'No se pudo exportar el reporte DIGEMID', 'error');
      },
      complete: () => {
        this.cargando = false;
      }
    });
  }

  private descargarArchivo(blob: Blob, nombreArchivo: string): void {
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = nombreArchivo;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);
  }
}