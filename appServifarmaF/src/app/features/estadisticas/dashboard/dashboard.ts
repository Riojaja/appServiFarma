import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';  // <--- Componente standalone
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { EstadisticaService } from '../../../core/services/estadistica';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    BaseChartDirective  // <--- Usar BaseChartDirective en lugar de NgChartsModule
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
  // Filtros de fechas
  fechaInicio: string = '';
  fechaFin: string = '';

  // Métricas
  totalVentas: number = 0;
  totalTransacciones: number = 0;
  ticketPromedio: number = 0;

  // ==============================
  // GRÁFICO DE BARRAS (Ventas por Día)
  // ==============================
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { display: true, position: 'top' },
      tooltip: { callbacks: { label: (context) => `S/ ${context.raw}` } }
    },
    scales: {
      y: { beginAtZero: true, ticks: { callback: (value) => `S/ ${value}` } }
    }
  };
  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      { data: [], label: 'Ventas por Día', backgroundColor: '#0d6efd' }
    ]
  };

  // ==============================
  // GRÁFICO DE PASTEL (Distribución por Medios de Pago)
  // ==============================
  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { position: 'top' },
      tooltip: { callbacks: { label: (context) => `${context.label}: S/ ${context.raw}` } }
    }
  };
  public pieChartType: ChartType = 'pie';
  public pieChartData: ChartData<'pie'> = {
    labels: [],
    datasets: [{ data: [], backgroundColor: ['#0d6efd', '#198754', '#ffc107', '#dc3545'] }]
  };

  // ==============================
  // GRÁFICO DE LÍNEAS (Ventas por Hora)
  // ==============================
  public lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { display: true, position: 'top' },
      tooltip: { callbacks: { label: (context) => `S/ ${context.raw}` } }
    },
    scales: {
      y: { beginAtZero: true, ticks: { callback: (value) => `S/ ${value}` } }
    }
  };
  public lineChartType: ChartType = 'line';
  public lineChartData: ChartData<'line'> = {
    labels: [],
    datasets: [
      { data: [], label: 'Ventas por Hora', borderColor: '#198754', backgroundColor: 'rgba(25, 135, 84, 0.1)', fill: true }
    ]
  };

  constructor(private estadisticaService: EstadisticaService) { }

  ngOnInit(): void {
    this.establecerFechasPorDefecto();
    this.cargarDatos();
  }

  establecerFechasPorDefecto(): void {
    const hoy = new Date();
    const primerDia = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
    this.fechaInicio = primerDia.toISOString().split('T')[0];
    this.fechaFin = ultimoDia.toISOString().split('T')[0];
  }

  cargarDatos(): void {
    this.cargarMetricas();
    this.cargarVentasPorDia();
    this.cargarDistribucionPagos();
    this.cargarVentasPorHora();
  }

  cargarMetricas(): void {
    // Total Ventas
    this.estadisticaService.obtenerTotalVentas(this.fechaInicio, this.fechaFin).subscribe({
      next: (data) => this.totalVentas = data,
      error: (err) => console.error('Error al cargar total ventas:', err)
    });

    // Total Transacciones
    this.estadisticaService.obtenerTotalTransacciones(this.fechaInicio, this.fechaFin).subscribe({
      next: (data) => this.totalTransacciones = data,
      error: (err) => console.error('Error al cargar total transacciones:', err)
    });

    // Ticket Promedio
    this.estadisticaService.obtenerTicketPromedio(this.fechaInicio, this.fechaFin).subscribe({
      next: (data) => this.ticketPromedio = data,
      error: (err) => console.error('Error al cargar ticket promedio:', err)
    });
  }

  cargarVentasPorDia(): void {
    this.estadisticaService.obtenerVentasPorDia(this.fechaInicio, this.fechaFin).subscribe({
      next: (data) => {
        this.barChartData.labels = data.map(item => item[0]);
        this.barChartData.datasets[0].data = data.map(item => item[1]);
      },
      error: (err) => console.error('Error al cargar ventas por día:', err)
    });
  }

  cargarDistribucionPagos(): void {
    this.estadisticaService.obtenerDistribucionPagos(this.fechaInicio, this.fechaFin).subscribe({
      next: (data) => {
        this.pieChartData.labels = data.map(item => item[0]);
        this.pieChartData.datasets[0].data = data.map(item => item[1]);
      },
      error: (err) => console.error('Error al cargar distribución de pagos:', err)
    });
  }

  cargarVentasPorHora(): void {
    // Usamos la fecha de inicio para mostrar las ventas por hora de ese día
    const fecha = this.fechaInicio;
    this.estadisticaService.obtenerVentasPorHora(fecha).subscribe({
      next: (data) => {
        this.lineChartData.labels = data.map(item => `${item[0]}:00`);
        this.lineChartData.datasets[0].data = data.map(item => item[1]);
      },
      error: (err) => console.error('Error al cargar ventas por hora:', err)
    });
  }
}