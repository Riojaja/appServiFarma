import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { ReporteService } from '../../../../core/services/reporte';
import { ReporteRentabilidad, RentabilidadCategoria } from '../../../../core/models/rentabilidad.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-rentabilidad',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './rentabilidad.html',
  styleUrls: ['./rentabilidad.css']
})
export class RentabilidadComponent implements OnInit {
  // Filtros
  fechaInicio: string = '';
  fechaFin: string = '';
  cargando: boolean = false;
  rentabilidad: ReporteRentabilidad | null = null;

  // KPIs
  get ingresosTotales(): number { return this.rentabilidad?.ingresosTotales || 0; }
  get costoVentas(): number { return this.rentabilidad?.costoVentas || 0; }
  get mermas(): number { return this.rentabilidad?.mermas || 0; }
  get margenBruto(): number { return this.rentabilidad?.margenBruto || 0; }
  get margenNeto(): number { return this.rentabilidad?.margenNeto || 0; }
  get categorias(): RentabilidadCategoria[] { return this.rentabilidad?.categorias || []; }

  // Gráfico
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { display: true, position: 'top', labels: { color: '#1e293b', font: { weight: 'bold' } } },
      tooltip: { callbacks: { label: (context) => `S/ ${(context.raw as number).toFixed(2)}` } }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { callback: (value) => `S/ ${value}`, color: '#64748b' },
        grid: { color: 'rgba(0,0,0,0.05)' }
      },
      x: {
        ticks: { color: '#64748b' },
        grid: { display: false }
      }
    }
  };
  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [{
      data: [],
      label: 'Margen por Categoría',
      backgroundColor: '#0d9488',
      borderRadius: 4,
      barPercentage: 0.6
    }]
  };

  constructor(
    private reporteService: ReporteService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.establecerFechasPorDefecto();
    this.cargarRentabilidad();
  }

  establecerFechasPorDefecto(): void {
    const hoy = new Date();
    const primerDia = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
    this.fechaInicio = this.formatDate(primerDia);
    this.fechaFin = this.formatDate(ultimoDia);
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  aplicarFiltroHoy(): void {
    const hoy = new Date();
    this.fechaInicio = this.formatDate(hoy);
    this.fechaFin = this.formatDate(hoy);
    this.cargarRentabilidad();
  }

  aplicarFiltroSemana(): void {
    const hoy = new Date();
    const dia = hoy.getDay();
    const diff = hoy.getDate() - dia + (dia === 0 ? -6 : 1);
    const lunes = new Date(hoy.setDate(diff));
    const domingo = new Date(hoy.setDate(lunes.getDate() + 6));
    this.fechaInicio = this.formatDate(lunes);
    this.fechaFin = this.formatDate(domingo);
    this.cargarRentabilidad();
  }

  aplicarFiltroMes(): void {
    this.establecerFechasPorDefecto();
    this.cargarRentabilidad();
  }

  cargarRentabilidad(): void {
    if (!this.fechaInicio || !this.fechaFin) {
      Swal.fire({ title: 'Error', text: 'Selecciona ambas fechas', icon: 'error', timer: 2000, toast: true, position: 'bottom-right' });
      return;
    }
    this.cargando = true;
    this.rentabilidad = null; // Reinicia los datos para evitar doble binding
    this.reporteService.obtenerRentabilidad(this.fechaInicio, this.fechaFin).subscribe({
      next: (data) => {
        this.rentabilidad = data;
        this.actualizarGrafico();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error:', err);
        Swal.fire({ title: 'Error', text: 'No se pudo cargar la rentabilidad', icon: 'error', timer: 2000, toast: true, position: 'bottom-right' });
        this.cargando = false;
      }
    });
  }

  private actualizarGrafico(): void {
    const cats = this.categorias;
    if (cats.length > 0) {
      this.barChartData.labels = cats.map(c => c.categoriaNombre);
      this.barChartData.datasets[0].data = cats.map(c => c.margen);
    } else {
      this.barChartData.labels = ['Sin datos'];
      this.barChartData.datasets[0].data = [0];
    }
    this.cdr.detectChanges();
  }

  exportar(formato: string): void {
    if (!this.fechaInicio || !this.fechaFin) {
      Swal.fire({ title: 'Error', text: 'Selecciona ambas fechas', icon: 'error', timer: 2000, toast: true, position: 'bottom-right' });
      return;
    }
    this.reporteService.exportarRentabilidad(this.fechaInicio, this.fechaFin, formato).subscribe({
      next: (blob) => {
        const ext = formato === 'pdf' ? 'pdf' : (formato === 'csv' ? 'csv' : 'xlsx');
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `rentabilidad_${this.fechaInicio}_${this.fechaFin}.${ext}`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(link.href);
        Swal.fire({ title: 'Éxito', text: 'Reporte exportado correctamente', icon: 'success', timer: 2000, toast: true, position: 'bottom-right' });
      },
      error: (err) => {
        console.error('Error al exportar:', err);
        Swal.fire({ title: 'Error', text: 'No se pudo exportar el reporte', icon: 'error', timer: 2000, toast: true, position: 'bottom-right' });
      }
    });
  }
}