import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { EstadisticaService } from '../../../core/services/estadistica';
import { AuthService } from '../../../core/auth';
import Swal from 'sweetalert2';
import * as jspdf from 'jspdf';
import 'jspdf-autotable';
import ExcelJS from 'exceljs';
import { saveAs } from 'file-saver';

export interface VentaPorDia {
  fecha: string;
  total: number;
}

export interface VentaPorHora {
  hora: number;
  total: number;
}

export interface DistribucionPago {
  medioPago: string;
  total: number;
}

export interface ResumenInterpretacion {
  mejorDia: string;
  mejorDiaTotal: number;
  peorDia: string;
  peorDiaTotal: number;
  horaPico: string;
  horaPicoTotal: number;
  medioPagoPrincipal: string;
  medioPagoPrincipalPct: number;
  promedioDiario: number;
  tendenciaTexto: string;
}

const COLOR_PRIMARIO = '#0d9488';
const COLOR_PRIMARIO_CLARO = '#2dd4bf';
const COLOR_PRIMARIO_OSCURO = '#0f766e';
const PALETA_PIE = ['#0d9488', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#6366f1'];

@Component({
  selector: 'app-dashboard-estadisticas',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, BaseChartDirective],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardEstadisticasComponent implements OnInit, OnDestroy {
  @ViewChild('barChartRef') barChartRef!: BaseChartDirective;
  @ViewChild('pieChartRef') pieChartRef!: BaseChartDirective;
  @ViewChild('lineChartRef') lineChartRef!: BaseChartDirective;

  private destroy$ = new Subject<void>();
  private debounceTimer: ReturnType<typeof setTimeout> | null = null;

  // ========== FILTROS ==========
  fechaInicio: string = '';
  fechaFin: string = '';
  cargando: boolean = false;
  exportandoPDF: boolean = false;
  exportandoExcel: boolean = false;
  hayDatos: boolean = false;
  errorCarga: string | null = null;

  // ========== MÉTRICAS ==========
  totalVentas: number = 0;
  totalTransacciones: number = 0;
  ticketPromedio: number = 0;
  totalVentasAnterior: number = 0;
  variacionPorcentual: number = 0;

  // ========== DATOS GRÁFICOS ==========
  ventasPorDiaData: VentaPorDia[] = [];
  ventasPorHoraData: VentaPorHora[] = [];
  distribucionPagosData: DistribucionPago[] = [];

  // ========== INTERPRETACIÓN AUTOMÁTICA ==========
  resumen: ResumenInterpretacion | null = null;

  /** Evita doble clic / llamadas concurrentes en cualquier operación pesada */
  get operacionEnCurso(): boolean {
    return this.cargando || this.exportandoPDF || this.exportandoExcel;
  }

  // ========== GRÁFICO BARRAS ==========
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 700, easing: 'easeOutQuart' },
    interaction: { mode: 'index', intersect: false },
    plugins: {
      legend: {
        display: true,
        position: 'top',
        align: 'end',
        labels: { color: '#1e293b', font: { weight: 'bold', size: 12 }, boxWidth: 14, usePointStyle: true }
      },
      tooltip: {
        backgroundColor: '#0f172a',
        titleColor: '#f8fafc',
        bodyColor: '#f1f5f9',
        padding: 10,
        cornerRadius: 8,
        displayColors: true,
        callbacks: {
          label: (context) => {
            const value = (context.raw as number) ?? 0;
            const label = context.dataset.label ?? '';
            return `${label}: S/ ${value.toFixed(2)}`;
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { callback: (value) => `S/ ${value}`, color: '#64748b', font: { size: 11 } },
        grid: { color: 'rgba(15,23,42,0.06)' }
      },
      x: {
        ticks: { color: '#64748b', font: { size: 11 } },
        grid: { display: false }
      }
    }
  };
  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Ventas del día',
        backgroundColor: (ctx: any) => this.gradienteBarras(ctx),
        hoverBackgroundColor: COLOR_PRIMARIO_OSCURO,
        borderRadius: 6,
        borderSkipped: false,
        barPercentage: 0.62,
        categoryPercentage: 0.7
      },
      {
        data: [],
        label: 'Promedio',
        type: 'line',
        borderColor: '#f59e0b',
        borderDash: [6, 4],
        borderWidth: 2,
        pointRadius: 0,
        fill: false,
        tension: 0
      } as any
    ]
  };

  // ========== GRÁFICO PASTEL (dona) ==========
  // Tipado como "any" a propósito: la propiedad "cutout" es válida en tiempo de
  // ejecución para gráficos doughnut, pero el genérico ChartConfiguration<'doughnut'>
  // no siempre lo resuelve bien según la versión de chart.js instalada.
  public pieChartOptions: any = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 700, easing: 'easeOutQuart' },
    cutout: '55%',
    plugins: {
      legend: {
        position: 'right',
        labels: { color: '#1e293b', font: { weight: 'bold', size: 12 }, boxWidth: 12, usePointStyle: true, padding: 14 }
      },
      tooltip: {
        backgroundColor: '#0f172a',
        titleColor: '#f8fafc',
        bodyColor: '#f1f5f9',
        padding: 10,
        cornerRadius: 8,
        callbacks: {
          label: (context: any) => {
            const label = context.label || '';
            const value = (context.raw as number) ?? 0;
            const dataArr = (context.dataset.data as number[]) || [];
            const suma = dataArr.reduce((a, b) => a + (b || 0), 0);
            const pct = suma > 0 ? ((value / suma) * 100).toFixed(1) : '0.0';
            return `${label}: S/ ${value.toFixed(2)} (${pct}%)`;
          }
        }
      }
    }
  };
  public pieChartType: ChartType = 'doughnut';
  public pieChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: PALETA_PIE,
        borderWidth: 3,
        borderColor: '#ffffff',
        hoverOffset: 10
      }
    ]
  };

  // ========== GRÁFICO LÍNEAS ==========
  public lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 700, easing: 'easeOutQuart' },
    interaction: { mode: 'index', intersect: false },
    plugins: {
      legend: { display: true, position: 'top', align: 'end', labels: { color: '#1e293b', font: { weight: 'bold', size: 12 }, usePointStyle: true } },
      tooltip: {
        backgroundColor: '#0f172a',
        titleColor: '#f8fafc',
        bodyColor: '#f1f5f9',
        padding: 10,
        cornerRadius: 8,
        callbacks: {
          label: (context) => {
            const value = (context.raw as number) ?? 0;
            return `S/ ${value.toFixed(2)}`;
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { callback: (value) => `S/ ${value}`, color: '#64748b', font: { size: 11 } },
        grid: { color: 'rgba(15,23,42,0.06)' }
      },
      x: {
        ticks: { color: '#64748b', font: { size: 11 } },
        grid: { display: false }
      }
    },
    elements: {
      line: { tension: 0.35, borderWidth: 2.5 },
      point: { radius: 3, hoverRadius: 6, backgroundColor: COLOR_PRIMARIO }
    }
  };
  public lineChartType: ChartType = 'line';
  public lineChartData: ChartData<'line'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Ventas por hora',
        fill: true,
        tension: 0.35,
        borderColor: COLOR_PRIMARIO,
        backgroundColor: (ctx: any) => this.gradienteLinea(ctx),
        pointBackgroundColor: COLOR_PRIMARIO,
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2
      }
    ]
  };

  constructor(
    private estadisticaService: EstadisticaService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.establecerFechasPorDefecto();
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    if (this.debounceTimer) { clearTimeout(this.debounceTimer); }
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ========== GRADIENTES (look "realista") ==========
  private gradienteBarras(context: any): string | CanvasGradient {
    const chart = context.chart;
    const { ctx, chartArea } = chart;
    if (!chartArea) { return COLOR_PRIMARIO; }
    const gradiente = ctx.createLinearGradient(0, chartArea.top, 0, chartArea.bottom);
    gradiente.addColorStop(0, COLOR_PRIMARIO_CLARO);
    gradiente.addColorStop(1, COLOR_PRIMARIO_OSCURO);
    return gradiente;
  }

  private gradienteLinea(context: any): string | CanvasGradient {
    const chart = context.chart;
    const { ctx, chartArea } = chart;
    if (!chartArea) { return 'rgba(13, 148, 136, 0.15)'; }
    const gradiente = ctx.createLinearGradient(0, chartArea.top, 0, chartArea.bottom);
    gradiente.addColorStop(0, 'rgba(13, 148, 136, 0.35)');
    gradiente.addColorStop(1, 'rgba(13, 148, 136, 0.02)');
    return gradiente;
  }

  // ========== FECHAS ==========
  establecerFechasPorDefecto(): void {
    const hoy = new Date();
    const primerDia = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
    this.fechaInicio = this.formatDate(primerDia);
    this.fechaFin = this.formatDate(ultimoDia);
  }

  private formatDate(date: Date): string {
    // OJO: no usar toISOString() aquí, porque convierte a UTC y puede
    // desfasar el día según la hora local (ej. Perú es UTC-5), enviando
    // al backend una fecha distinta a la que el usuario ve en pantalla.
    const anio = date.getFullYear();
    const mes = String(date.getMonth() + 1).padStart(2, '0');
    const dia = String(date.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }

  /** Se dispara al cambiar manualmente los inputs de fecha (con debounce, evita ráfagas de peticiones) */
  onFechaCambiada(): void {
    if (this.debounceTimer) { clearTimeout(this.debounceTimer); }
    this.debounceTimer = setTimeout(() => {
      if (this.fechaInicio && this.fechaFin) {
        this.aplicarFiltroPersonalizado();
      }
    }, 450);
  }

  // ========== BOTONES RÁPIDOS ==========
  aplicarFiltroHoy(): void {
    if (this.operacionEnCurso) { return; }
    const hoy = new Date();
    this.fechaInicio = this.formatDate(hoy);
    this.fechaFin = this.formatDate(hoy);
    this.cargarDatos();
  }

  aplicarFiltroSemana(): void {
    if (this.operacionEnCurso) { return; }
    const hoy = new Date();
    const dia = hoy.getDay();
    const diff = hoy.getDate() - dia + (dia === 0 ? -6 : 1);
    const base = new Date(hoy);
    const lunes = new Date(base.setDate(diff));
    const domingo = new Date(new Date(lunes).setDate(lunes.getDate() + 6));
    this.fechaInicio = this.formatDate(lunes);
    this.fechaFin = this.formatDate(domingo);
    this.cargarDatos();
  }

  aplicarFiltroMes(): void {
    if (this.operacionEnCurso) { return; }
    this.establecerFechasPorDefecto();
    this.cargarDatos();
  }

  aplicarFiltroPersonalizado(): void {
    if (this.operacionEnCurso) { return; }
    if (!this.fechaInicio || !this.fechaFin) {
      Swal.fire('Advertencia', 'Selecciona ambas fechas', 'warning');
      return;
    }
    if (new Date(this.fechaInicio) > new Date(this.fechaFin)) {
      Swal.fire('Advertencia', 'La fecha de inicio no puede ser mayor a la fecha fin', 'warning');
      return;
    }
    this.cargarDatos();
  }

  // ========== CARGA DE DATOS ==========
  cargarDatos(): void {
    // Guard anti doble-clic / doble ejecución
    if (this.cargando) { return; }
    if (!this.fechaInicio || !this.fechaFin) {
      Swal.fire('Error', 'Rango de fechas inválido', 'error');
      return;
    }

    this.cargando = true;
    this.errorCarga = null;
    this.hayDatos = false;
    this.cdr.detectChanges();

    Promise.all([
      this.cargarMetricas(),
      this.cargarVentasPorDia(),
      this.cargarDistribucionPagos(),
      this.cargarVentasPorHora()
    ])
      .then(() => {
        this.calcularInterpretacion();
      })
      .catch((err) => {
        console.error('Error general al cargar el dashboard:', err);
        this.errorCarga = 'Ocurrió un problema cargando algunas estadísticas.';
      })
      .finally(() => {
        this.cargando = false;
        this.hayDatos = this.ventasPorDiaData.length > 0 ||
                        this.distribucionPagosData.length > 0 ||
                        this.ventasPorHoraData.length > 0;
        this.cdr.detectChanges();
      });
  }

  // ========== MÉTRICAS ==========
  private cargarMetricas(): Promise<void> {
    return new Promise((resolve) => {
      this.estadisticaService.obtenerTotalVentas(this.fechaInicio, this.fechaFin)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data) => {
            this.totalVentas = data || 0;
            this.cargarTransaccionesYTicket(resolve);
          },
          error: (err) => {
            console.error('Error total ventas:', err);
            this.totalVentas = 0;
            this.cargarTransaccionesYTicket(resolve);
          }
        });
    });
  }

  private cargarTransaccionesYTicket(resolve: () => void): void {
    Promise.all([
      this.estadisticaService.obtenerTotalTransacciones(this.fechaInicio, this.fechaFin).toPromise(),
      this.estadisticaService.obtenerTicketPromedio(this.fechaInicio, this.fechaFin).toPromise()
    ]).then(([transacciones, ticket]) => {
      this.totalTransacciones = transacciones || 0;
      this.ticketPromedio = ticket || 0;
      this.cargarVariacion();
      resolve();
    }).catch((err) => {
      console.error('Error en métricas:', err);
      this.totalTransacciones = 0;
      this.ticketPromedio = 0;
      resolve();
    });
  }

  private cargarVariacion(): void {
    try {
      const inicio = new Date(this.fechaInicio);
      const fin = new Date(this.fechaFin);
      const diffDias = Math.ceil((fin.getTime() - inicio.getTime()) / (1000 * 60 * 60 * 24)) + 1;
      const inicioAnterior = new Date(inicio);
      inicioAnterior.setDate(inicioAnterior.getDate() - diffDias);
      const finAnterior = new Date(inicio);
      finAnterior.setDate(finAnterior.getDate() - 1);

      const inicioStr = this.formatDate(inicioAnterior);
      const finStr = this.formatDate(finAnterior);

      this.estadisticaService.obtenerTotalVentas(inicioStr, finStr)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data) => {
            this.totalVentasAnterior = data || 0;
            if (this.totalVentasAnterior > 0) {
              this.variacionPorcentual = ((this.totalVentas - this.totalVentasAnterior) / this.totalVentasAnterior) * 100;
            } else {
              this.variacionPorcentual = this.totalVentas > 0 ? 100 : 0;
            }
            this.cdr.detectChanges();
          },
          error: () => {
            this.totalVentasAnterior = 0;
            this.variacionPorcentual = 0;
          }
        });
    } catch (e) {
      this.totalVentasAnterior = 0;
      this.variacionPorcentual = 0;
    }
  }

  // ========== VENTAS POR DÍA ==========
  private cargarVentasPorDia(): Promise<void> {
    return new Promise((resolve) => {
      this.estadisticaService.obtenerVentasPorDia(this.fechaInicio, this.fechaFin)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data: any[]) => {
            this.ventasPorDiaData = (data && data.length > 0)
              ? data.map(item => ({ fecha: item[0] || '', total: item[1] || 0 }))
              : [];
            this.actualizarGraficoBarras();
            resolve();
          },
          error: (err) => {
            console.error('Error ventas por día:', err);
            this.ventasPorDiaData = [];
            this.actualizarGraficoBarras();
            resolve();
          }
        });
    });
  }

  private actualizarGraficoBarras(): void {
    if (this.ventasPorDiaData.length > 0) {
      const promedio = this.ventasPorDiaData.reduce((acc, i) => acc + i.total, 0) / this.ventasPorDiaData.length;
      this.barChartData = {
        labels: this.ventasPorDiaData.map(item => item.fecha),
        datasets: [
          { ...this.barChartData.datasets[0], data: this.ventasPorDiaData.map(item => item.total) },
          { ...this.barChartData.datasets[1], data: this.ventasPorDiaData.map(() => promedio) }
        ]
      };
    } else {
      this.barChartData = {
        labels: ['Sin datos'],
        datasets: [
          { ...this.barChartData.datasets[0], data: [0] },
          { ...this.barChartData.datasets[1], data: [0] }
        ]
      };
    }
    this.barChartRef?.update();
    this.cdr.detectChanges();
  }

  // ========== DISTRIBUCIÓN PAGOS ==========
  private cargarDistribucionPagos(): Promise<void> {
    return new Promise((resolve) => {
      this.estadisticaService.obtenerDistribucionPagos(this.fechaInicio, this.fechaFin)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data: any[]) => {
            this.distribucionPagosData = (data && data.length > 0)
              ? data.map(item => ({ medioPago: item[0] || '', total: item[1] || 0 }))
              : [];
            this.actualizarGraficoPie();
            resolve();
          },
          error: (err) => {
            console.error('Error distribución pagos:', err);
            this.distribucionPagosData = [];
            this.actualizarGraficoPie();
            resolve();
          }
        });
    });
  }

  private actualizarGraficoPie(): void {
    if (this.distribucionPagosData.length > 0) {
      const colors = this.distribucionPagosData.map((_, i) => PALETA_PIE[i % PALETA_PIE.length]);
      this.pieChartData = {
        labels: this.distribucionPagosData.map(item => item.medioPago),
        datasets: [{ ...this.pieChartData.datasets[0], data: this.distribucionPagosData.map(item => item.total), backgroundColor: colors }]
      };
    } else {
      this.pieChartData = {
        labels: ['Sin datos'],
        datasets: [{ ...this.pieChartData.datasets[0], data: [0], backgroundColor: ['#e5e7eb'] }]
      };
    }
    this.pieChartRef?.update();
    this.cdr.detectChanges();
  }

  // ========== VENTAS POR HORA ==========
  private cargarVentasPorHora(): Promise<void> {
    const fecha = this.fechaInicio;
    return new Promise((resolve) => {
      this.estadisticaService.obtenerVentasPorHora(fecha)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data: any[]) => {
            this.ventasPorHoraData = (data && data.length > 0)
              ? data.map(item => ({ hora: item[0] || 0, total: item[1] || 0 }))
              : [];
            this.actualizarGraficoLineas();
            resolve();
          },
          error: (err) => {
            console.error('Error ventas por hora:', err);
            this.ventasPorHoraData = [];
            this.actualizarGraficoLineas();
            resolve();
          }
        });
    });
  }

  private actualizarGraficoLineas(): void {
    if (this.ventasPorHoraData.length > 0) {
      this.lineChartData = {
        labels: this.ventasPorHoraData.map(item => `${item.hora}:00`),
        datasets: [{ ...this.lineChartData.datasets[0], data: this.ventasPorHoraData.map(item => item.total) }]
      };
    } else {
      this.lineChartData = {
        labels: ['Sin datos'],
        datasets: [{ ...this.lineChartData.datasets[0], data: [0] }]
      };
    }
    this.lineChartRef?.update();
    this.cdr.detectChanges();
  }

  // ========== INTERPRETACIÓN AUTOMÁTICA DE LOS GRÁFICOS ==========
  private calcularInterpretacion(): void {
    if (this.ventasPorDiaData.length === 0 && this.distribucionPagosData.length === 0 && this.ventasPorHoraData.length === 0) {
      this.resumen = null;
      return;
    }

    let mejorDia = '—', peorDia = '—';
    let mejorDiaTotal = 0, peorDiaTotal = 0;
    let promedioDiario = 0;

    if (this.ventasPorDiaData.length > 0) {
      const ordenado = [...this.ventasPorDiaData].sort((a, b) => b.total - a.total);
      mejorDia = ordenado[0].fecha;
      mejorDiaTotal = ordenado[0].total;
      peorDia = ordenado[ordenado.length - 1].fecha;
      peorDiaTotal = ordenado[ordenado.length - 1].total;
      promedioDiario = this.ventasPorDiaData.reduce((a, b) => a + b.total, 0) / this.ventasPorDiaData.length;
    }

    let horaPico = '—';
    let horaPicoTotal = 0;
    if (this.ventasPorHoraData.length > 0) {
      const ordenadoHoras = [...this.ventasPorHoraData].sort((a, b) => b.total - a.total);
      horaPico = `${ordenadoHoras[0].hora}:00`;
      horaPicoTotal = ordenadoHoras[0].total;
    }

    let medioPagoPrincipal = '—';
    let medioPagoPrincipalPct = 0;
    if (this.distribucionPagosData.length > 0) {
      const total = this.distribucionPagosData.reduce((a, b) => a + b.total, 0);
      const top = [...this.distribucionPagosData].sort((a, b) => b.total - a.total)[0];
      medioPagoPrincipal = top.medioPago;
      medioPagoPrincipalPct = total > 0 ? (top.total / total) * 100 : 0;
    }

    const tendenciaTexto = this.variacionPorcentual >= 0
      ? `las ventas subieron ${Math.abs(this.variacionPorcentual).toFixed(1)}% respecto al período anterior`
      : `las ventas bajaron ${Math.abs(this.variacionPorcentual).toFixed(1)}% respecto al período anterior`;

    this.resumen = {
      mejorDia, mejorDiaTotal, peorDia, peorDiaTotal,
      horaPico, horaPicoTotal,
      medioPagoPrincipal, medioPagoPrincipalPct,
      promedioDiario, tendenciaTexto
    };
  }

  // ========== UTILIDADES VISTA ==========
  getVariacionColor(): string {
    return this.variacionPorcentual >= 0 ? 'text-success' : 'text-danger';
  }

  getVariacionIcono(): string {
    return this.variacionPorcentual >= 0 ? 'bi-arrow-up' : 'bi-arrow-down';
  }

  getVariacionTexto(): string {
    return `${Math.abs(this.variacionPorcentual).toFixed(1)}%`;
  }

  // ========== EXPORTAR A PDF (con gráficos incluidos) ==========
  async exportarPDF(): Promise<void> {
    if (this.operacionEnCurso) { return; }
    if (!this.hayDatos) {
      Swal.fire('Sin datos', 'No hay información para exportar en el período seleccionado.', 'info');
      return;
    }

    this.exportandoPDF = true;
    Swal.fire({ title: 'Generando PDF...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

    try {
      const doc = new jspdf.jsPDF('landscape', 'mm', 'a4');
      const pageWidth = doc.internal.pageSize.getWidth();
      const pageHeight = doc.internal.pageSize.getHeight();

      doc.setFillColor('#0d9488');
      doc.rect(0, 0, pageWidth, 16, 'F');
      doc.setFontSize(16);
      doc.setTextColor('#ffffff');
      doc.text('ServiFarma - Reporte de Estadísticas', pageWidth / 2, 10, { align: 'center' });

      doc.setFontSize(10);
      doc.setTextColor('#475569');
      doc.text(`Período: ${this.fechaInicio} al ${this.fechaFin}  •  Generado: ${new Date().toLocaleString('es-PE')}`, pageWidth / 2, 23, { align: 'center' });

      const metrics = [
        { label: 'Total Ventas', value: `S/ ${this.totalVentas.toFixed(2)}` },
        { label: 'Transacciones', value: `${this.totalTransacciones}` },
        { label: 'Ticket Promedio', value: `S/ ${this.ticketPromedio.toFixed(2)}` },
        { label: 'Variación', value: `${this.variacionPorcentual >= 0 ? '+' : ''}${this.variacionPorcentual.toFixed(1)}%` }
      ];

      let y = 32;
      doc.setFontSize(11);
      metrics.forEach((m, i) => {
        doc.setTextColor('#1e293b');
        doc.text(m.label, 20 + i * 68, y);
        doc.setTextColor('#0d9488');
        doc.setFontSize(13);
        doc.text(m.value, 20 + i * 68, y + 6);
        doc.setFontSize(11);
      });

      y += 14;

      // ---- Gráficos como imágenes ----
      const imgBar = this.barChartRef?.chart?.toBase64Image('image/png', 1);
      const imgPie = this.pieChartRef?.chart?.toBase64Image('image/png', 1);
      const imgLine = this.lineChartRef?.chart?.toBase64Image('image/png', 1);

      const anchoImg = (pageWidth - 50) / 2;
      const altoImg = 62;

      if (imgBar) {
        doc.setFontSize(11);
        doc.setTextColor('#1e293b');
        doc.text('Ventas por Día', 20, y);
        doc.addImage(imgBar, 'PNG', 20, y + 3, anchoImg, altoImg);
      }
      if (imgPie) {
        doc.text('Medios de Pago', 20 + anchoImg + 10, y);
        doc.addImage(imgPie, 'PNG', 20 + anchoImg + 10, y + 3, anchoImg, altoImg);
      }

      y += altoImg + 12;

      if (imgLine) {
        doc.text(`Ventas por Hora (${this.fechaInicio})`, 20, y);
        doc.addImage(imgLine, 'PNG', 20, y + 3, pageWidth - 40, altoImg);
        y += altoImg + 12;
      }

      // ---- Interpretación automática ----
      if (this.resumen) {
        if (y > pageHeight - 45) { doc.addPage(); y = 20; }
        doc.setFillColor('#f0fdfa');
        doc.roundedRect(18, y, pageWidth - 36, 34, 3, 3, 'F');
        doc.setFontSize(12);
        doc.setTextColor('#0d9488');
        doc.text('Interpretación de resultados', 24, y + 8);
        doc.setFontSize(9.5);
        doc.setTextColor('#334155');
        const texto =
          `El mejor día de ventas fue ${this.resumen.mejorDia} (S/ ${this.resumen.mejorDiaTotal.toFixed(2)}), mientras que ${this.resumen.peorDia} ` +
          `registró el monto más bajo (S/ ${this.resumen.peorDiaTotal.toFixed(2)}). La hora de mayor actividad fue ${this.resumen.horaPico} ` +
          `con S/ ${this.resumen.horaPicoTotal.toFixed(2)} en ventas. El medio de pago más usado fue "${this.resumen.medioPagoPrincipal}" ` +
          `(${this.resumen.medioPagoPrincipalPct.toFixed(1)}% del total). En general, ${this.resumen.tendenciaTexto}.`;
        const lineas = doc.splitTextToSize(texto, pageWidth - 48);
        doc.text(lineas, 24, y + 16);
        y += 40;
      }

      // ---- Tablas de detalle ----
      if (this.ventasPorDiaData.length > 0) {
        if (y > pageHeight - 30) { doc.addPage(); y = 20; }
        (doc as any).autoTable({
          startY: y,
          head: [['Fecha', 'Total Ventas']],
          body: this.ventasPorDiaData.map(item => [item.fecha, `S/ ${item.total.toFixed(2)}`]),
          theme: 'striped',
          headStyles: { fillColor: '#0d9488', textColor: '#fff' },
          styles: { fontSize: 8 },
          margin: { left: 20, right: 20 }
        });
      }

      doc.save(`Estadisticas_${this.fechaInicio}_al_${this.fechaFin}.pdf`);
      Swal.close();
      this.notificarExito('PDF generado correctamente');
    } catch (err) {
      console.error('Error exportando PDF:', err);
      Swal.close();
      Swal.fire('Error', 'No se pudo generar el PDF. Intenta nuevamente.', 'error');
    } finally {
      this.exportandoPDF = false;
    }
  }

  // ========== EXPORTAR A EXCEL (con gráficos incluidos) ==========
  async exportarExcel(): Promise<void> {
    if (this.operacionEnCurso) { return; }
    if (!this.hayDatos) {
      Swal.fire('Sin datos', 'No hay información para exportar en el período seleccionado.', 'info');
      return;
    }

    this.exportandoExcel = true;
    Swal.fire({ title: 'Generando Excel...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

    try {
      const workbook = new ExcelJS.Workbook();
      workbook.creator = 'ServiFarma';
      workbook.created = new Date();

      const hoja = workbook.addWorksheet('Estadísticas', {
        views: [{ showGridLines: false }]
      });

      hoja.mergeCells('A1:F1');
      hoja.getCell('A1').value = 'ServiFarma - Reporte de Estadísticas';
      hoja.getCell('A1').font = { size: 16, bold: true, color: { argb: 'FF0D9488' } };

      hoja.mergeCells('A2:F2');
      hoja.getCell('A2').value = `Período: ${this.fechaInicio} al ${this.fechaFin}`;
      hoja.getCell('A2').font = { size: 10, color: { argb: 'FF475569' } };

      // ---- KPIs ----
      let fila = 4;
      const kpis: [string, string | number][] = [
        ['Total Ventas', this.totalVentas],
        ['Transacciones', this.totalTransacciones],
        ['Ticket Promedio', this.ticketPromedio],
        ['Variación vs. período anterior', `${this.variacionPorcentual.toFixed(1)}%`]
      ];
      kpis.forEach(([label, value]) => {
        hoja.getCell(`A${fila}`).value = label;
        hoja.getCell(`A${fila}`).font = { bold: true };
        hoja.getCell(`B${fila}`).value = value;
        hoja.getCell(`B${fila}`).font = { color: { argb: 'FF0D9488' }, bold: true };
        fila++;
      });
      fila += 1;

      // ---- Interpretación ----
      if (this.resumen) {
        hoja.mergeCells(`A${fila}:F${fila}`);
        hoja.getCell(`A${fila}`).value = 'Interpretación de resultados';
        hoja.getCell(`A${fila}`).font = { bold: true, size: 12, color: { argb: 'FF0D9488' } };
        fila++;
        const texto =
          `El mejor día de ventas fue ${this.resumen.mejorDia} (S/ ${this.resumen.mejorDiaTotal.toFixed(2)}), mientras que ${this.resumen.peorDia} ` +
          `registró el monto más bajo (S/ ${this.resumen.peorDiaTotal.toFixed(2)}). La hora de mayor actividad fue ${this.resumen.horaPico} ` +
          `con S/ ${this.resumen.horaPicoTotal.toFixed(2)} en ventas. El medio de pago más usado fue "${this.resumen.medioPagoPrincipal}" ` +
          `(${this.resumen.medioPagoPrincipalPct.toFixed(1)}% del total). En general, ${this.resumen.tendenciaTexto}.`;
        hoja.mergeCells(`A${fila}:F${fila + 2}`);
        const celdaTexto = hoja.getCell(`A${fila}`);
        celdaTexto.value = texto;
        celdaTexto.alignment = { wrapText: true, vertical: 'top' };
        fila += 4;
      }

      // ---- Tabla ventas por día ----
      if (this.ventasPorDiaData.length > 0) {
        hoja.getCell(`A${fila}`).value = 'Ventas por Día';
        hoja.getCell(`A${fila}`).font = { bold: true };
        fila++;
        hoja.getCell(`A${fila}`).value = 'Fecha';
        hoja.getCell(`B${fila}`).value = 'Total';
        hoja.getRow(fila).font = { bold: true };
        hoja.getRow(fila).eachCell((c: any) => c.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF0D9488' } });
        fila++;
        this.ventasPorDiaData.forEach(item => {
          hoja.getCell(`A${fila}`).value = item.fecha;
          hoja.getCell(`B${fila}`).value = item.total;
          fila++;
        });
        fila += 1;
      }

      // ---- Tabla distribución de pagos ----
      if (this.distribucionPagosData.length > 0) {
        hoja.getCell(`A${fila}`).value = 'Distribución por Medio de Pago';
        hoja.getCell(`A${fila}`).font = { bold: true };
        fila++;
        hoja.getCell(`A${fila}`).value = 'Medio de Pago';
        hoja.getCell(`B${fila}`).value = 'Total';
        hoja.getRow(fila).font = { bold: true };
        fila++;
        this.distribucionPagosData.forEach(item => {
          hoja.getCell(`A${fila}`).value = item.medioPago;
          hoja.getCell(`B${fila}`).value = item.total;
          fila++;
        });
        fila += 1;
      }

      // ---- Tabla ventas por hora ----
      if (this.ventasPorHoraData.length > 0) {
        hoja.getCell(`A${fila}`).value = 'Ventas por Hora';
        hoja.getCell(`A${fila}`).font = { bold: true };
        fila++;
        hoja.getCell(`A${fila}`).value = 'Hora';
        hoja.getCell(`B${fila}`).value = 'Total';
        hoja.getRow(fila).font = { bold: true };
        fila++;
        this.ventasPorHoraData.forEach(item => {
          hoja.getCell(`A${fila}`).value = `${item.hora}:00`;
          hoja.getCell(`B${fila}`).value = item.total;
          fila++;
        });
        fila += 2;
      }

      hoja.getColumn('A').width = 28;
      hoja.getColumn('B').width = 18;

      // ---- Gráficos como imágenes embebidas ----
      const filaGraficos = fila + 1;
      const agregarImagen = (base64: string | undefined, celda: string, rangoFin: string) => {
        if (!base64) { return; }
        const id = workbook.addImage({ base64: base64.split(',')[1], extension: 'png' });
        hoja.addImage(id, `${celda}:${rangoFin}`);
      };

      const imgBar = this.barChartRef?.chart?.toBase64Image('image/png', 1);
      const imgPie = this.pieChartRef?.chart?.toBase64Image('image/png', 1);
      const imgLine = this.lineChartRef?.chart?.toBase64Image('image/png', 1);

      hoja.getCell(`A${filaGraficos}`).value = 'Ventas por Día (gráfico)';
      hoja.getCell(`A${filaGraficos}`).font = { bold: true };
      agregarImagen(imgBar, `A${filaGraficos + 1}`, `F${filaGraficos + 16}`);

      hoja.getCell(`H${filaGraficos}`).value = 'Medios de Pago (gráfico)';
      hoja.getCell(`H${filaGraficos}`).font = { bold: true };
      agregarImagen(imgPie, `H${filaGraficos + 1}`, `M${filaGraficos + 16}`);

      const filaLinea = filaGraficos + 19;
      hoja.getCell(`A${filaLinea}`).value = `Ventas por Hora (gráfico) - ${this.fechaInicio}`;
      hoja.getCell(`A${filaLinea}`).font = { bold: true };
      agregarImagen(imgLine, `A${filaLinea + 1}`, `M${filaLinea + 16}`);

      const buffer = await workbook.xlsx.writeBuffer();
      const blob = new Blob([buffer], { type: 'application/octet-stream' });
      saveAs(blob, `Estadisticas_${this.fechaInicio}_al_${this.fechaFin}.xlsx`);

      Swal.close();
      this.notificarExito('Excel generado correctamente');
    } catch (err) {
      console.error('Error exportando Excel:', err);
      Swal.close();
      Swal.fire('Error', 'No se pudo generar el Excel. Intenta nuevamente.', 'error');
    } finally {
      this.exportandoExcel = false;
    }
  }

  private notificarExito(mensaje: string): void {
    Swal.fire({
      toast: true,
      position: 'top-end',
      icon: 'success',
      title: mensaje,
      showConfirmButton: false,
      timer: 2500,
      timerProgressBar: true
    });
  }
}