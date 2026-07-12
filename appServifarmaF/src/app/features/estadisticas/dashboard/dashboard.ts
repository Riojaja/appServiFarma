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
import jspdfAutotable from 'jspdf-autotable';

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

type FiltroActivo = 'hoy' | 'semana' | 'mes' | 'personalizado';

const COLOR_PRIMARIO = '#0d9488';
const COLOR_PRIMARIO_CLARO = '#2dd4bf';
const COLOR_PRIMARIO_OSCURO = '#0f766e';
const COLOR_NAVY = '#1a1a2e';
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
  filtroActivo: FiltroActivo = 'mes';
  cargando: boolean = false;
  /** 'actual' | 'hoy' mientras se genera ese PDF específico; null si no hay ninguno en curso */
  exportando: 'actual' | 'hoy' | null = null;
  hayDatos: boolean = false;
  errorCarga: string | null = null;

  /** Evita doble-click / llamadas concurrentes en cualquier operación pesada */
  get operacionEnCurso(): boolean {
    return this.cargando || this.exportando !== null;
  }

  /** Texto humano del período actualmente seleccionado, ej: "Mes de Julio de 2026" */
  get periodoTitulo(): string {
    switch (this.filtroActivo) {
      case 'hoy':
        return `Hoy: ${this.humanizeDate(this.fechaInicio)}`;
      case 'semana':
        return `Semana del ${this.humanizeDate(this.fechaInicio, true)} al ${this.humanizeDate(this.fechaFin, true)}`;
      case 'mes':
        return `Mes de ${this.humanizeMonth(this.fechaInicio)} de ${this.parseLocalDate(this.fechaInicio).getFullYear()}`;
      default:
        return `Del ${this.humanizeDate(this.fechaInicio, true)} al ${this.humanizeDate(this.fechaFin, true)}`;
    }
  }

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

  // ========== INTERPRETACIÓN AUTOMÁTICA (solo se usa para el PDF y los mini-KPI) ==========
  resumen: ResumenInterpretacion | null = null;

  // ========== GRÁFICO BARRAS ==========
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 500, easing: 'easeOutQuart' },
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

  // ========== GRÁFICO DONA (medios de pago) ==========
  public pieChartOptions: any = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 500, easing: 'easeOutQuart' },
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
      { data: [], backgroundColor: PALETA_PIE, borderWidth: 3, borderColor: '#ffffff', hoverOffset: 10 }
    ]
  };

  // ========== GRÁFICO LÍNEAS (ventas por hora) ==========
  public lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 500, easing: 'easeOutQuart' },
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
          label: (context) => `S/ ${((context.raw as number) ?? 0).toFixed(2)}`
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
    this.filtroActivo = 'mes';
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

  // ========== FECHAS (parseadas en LOCAL, no UTC, para evitar el bug del "día equivocado") ==========
  establecerFechasPorDefecto(): void {
    const hoy = new Date();
    this.fechaInicio = this.formatDate(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
    this.fechaFin = this.formatDate(new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0));
  }

  private formatDate(date: Date): string {
    const anio = date.getFullYear();
    const mes = String(date.getMonth() + 1).padStart(2, '0');
    const dia = String(date.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }

  /** Convierte "YYYY-MM-DD" a Date en horario LOCAL (evita el corrimiento de día por UTC) */
  private parseLocalDate(fecha: string): Date {
    if (!fecha) return new Date();
    const soloFecha = fecha.split('T')[0];
    const [anio, mes, dia] = soloFecha.split('-').map(Number);
    return new Date(anio, (mes || 1) - 1, dia || 1);
  }

  /** "Domingo, 12 de julio de 2026" (o "Domingo 12 de julio" con soloDia=true) */
  humanizeDate(fecha: string, soloDia: boolean = false): string {
    if (!fecha) return '—';
    const dateObj = this.parseLocalDate(fecha);
    const diasSemana = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];
    const meses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];
    const diaSemana = diasSemana[dateObj.getDay()];
    const dia = dateObj.getDate();
    const mes = meses[dateObj.getMonth()];
    const anio = dateObj.getFullYear();
    return soloDia ? `${diaSemana} ${dia} de ${mes}` : `${diaSemana}, ${dia} de ${mes} de ${anio}`;
  }

  /** "Julio" */
  humanizeMonth(fecha: string): string {
    if (!fecha) return '—';
    const dateObj = this.parseLocalDate(fecha);
    const meses = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    return meses[dateObj.getMonth()];
  }

  /** Se dispara al cambiar manualmente los inputs de fecha (con debounce, evita ráfagas de peticiones) */
  onFechaCambiada(): void {
    this.filtroActivo = 'personalizado';
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
    this.filtroActivo = 'hoy';
    this.cargarDatos();
  }

  aplicarFiltroSemana(): void {
    if (this.operacionEnCurso) { return; }
    const hoy = new Date();
    const dia = hoy.getDay();
    const diff = hoy.getDate() - dia + (dia === 0 ? -6 : 1);
    const lunes = new Date(hoy.getFullYear(), hoy.getMonth(), diff);
    const domingo = new Date(hoy.getFullYear(), hoy.getMonth(), diff + 6);
    this.fechaInicio = this.formatDate(lunes);
    this.fechaFin = this.formatDate(domingo);
    this.filtroActivo = 'semana';
    this.cargarDatos();
  }

  aplicarFiltroMes(): void {
    if (this.operacionEnCurso) { return; }
    const hoy = new Date();
    this.fechaInicio = this.formatDate(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
    this.fechaFin = this.formatDate(new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0));
    this.filtroActivo = 'mes';
    this.cargarDatos();
  }

  aplicarFiltroPersonalizado(): void {
    if (this.operacionEnCurso) { return; }
    if (!this.fechaInicio || !this.fechaFin) {
      Swal.fire('Advertencia', 'Selecciona ambas fechas', 'warning');
      return;
    }
    if (this.parseLocalDate(this.fechaInicio) > this.parseLocalDate(this.fechaFin)) {
      Swal.fire('Advertencia', 'La fecha de inicio no puede ser mayor a la fecha fin', 'warning');
      return;
    }
    this.filtroActivo = 'personalizado';
    this.cargarDatos();
  }

  // ========== CARGA DE DATOS ==========
  // Importante: cada sub-carga SIEMPRE resuelve su Promise (nunca la rechaza),
  // así un error puntual en un endpoint no tumba el resto del dashboard ni deja
  // el botón pegado en "Cargando...".
  async cargarDatos(): Promise<void> {
    if (this.cargando) { return; }
    if (!this.fechaInicio || !this.fechaFin) {
      Swal.fire('Error', 'Rango de fechas inválido', 'error');
      return;
    }

    this.cargando = true;
    this.errorCarga = null;
    this.cdr.detectChanges();

    try {
      await Promise.all([
        this.cargarMetricas(),
        this.cargarVentasPorDia(),
        this.cargarDistribucionPagos(),
        this.cargarVentasPorHora()
      ]);
      this.calcularInterpretacion();
    } catch (err) {
      console.error('Error general al cargar el dashboard:', err);
      this.errorCarga = 'Ocurrió un problema cargando algunas estadísticas.';
    } finally {
      this.cargando = false;
      this.hayDatos = this.ventasPorDiaData.length > 0 ||
                      this.distribucionPagosData.length > 0 ||
                      this.ventasPorHoraData.length > 0;
      this.cdr.detectChanges();
    }
  }

  // ========== MÉTRICAS ==========
  private cargarMetricas(): Promise<void> {
    return new Promise((resolve) => {
      this.estadisticaService.obtenerTotalVentas(this.fechaInicio, this.fechaFin)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data) => {
            this.totalVentas = data || 0;
            this.cargarTransaccionesYTicket().finally(resolve);
          },
          error: (err) => {
            console.error('Error total ventas:', err);
            this.totalVentas = 0;
            this.cargarTransaccionesYTicket().finally(resolve);
          }
        });
    });
  }

  private async cargarTransaccionesYTicket(): Promise<void> {
    try {
      const [transacciones, ticket] = await Promise.all([
        this.estadisticaService.obtenerTotalTransacciones(this.fechaInicio, this.fechaFin).toPromise(),
        this.estadisticaService.obtenerTicketPromedio(this.fechaInicio, this.fechaFin).toPromise()
      ]);
      this.totalTransacciones = transacciones || 0;
      this.ticketPromedio = ticket || 0;
    } catch (err) {
      console.error('Error en métricas:', err);
      this.totalTransacciones = 0;
      this.ticketPromedio = 0;
    }
    this.cargarVariacion();
  }

  private cargarVariacion(): void {
    try {
      const inicio = this.parseLocalDate(this.fechaInicio);
      const fin = this.parseLocalDate(this.fechaFin);
      const diffDias = Math.ceil((fin.getTime() - inicio.getTime()) / (1000 * 60 * 60 * 24)) + 1;
      const inicioAnterior = new Date(inicio);
      inicioAnterior.setDate(inicioAnterior.getDate() - diffDias);
      const finAnterior = new Date(inicio);
      finAnterior.setDate(finAnterior.getDate() - 1);

      this.estadisticaService.obtenerTotalVentas(this.formatDate(inicioAnterior), this.formatDate(finAnterior))
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data) => {
            this.totalVentasAnterior = data || 0;
            this.variacionPorcentual = this.totalVentasAnterior > 0
              ? ((this.totalVentas - this.totalVentasAnterior) / this.totalVentasAnterior) * 100
              : (this.totalVentas > 0 ? 100 : 0);
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
            this.ventasPorDiaData = (data || []).map(item => ({ fecha: item[0] || '', total: item[1] || 0 }));
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
        labels: this.ventasPorDiaData.map(item => this.humanizeDate(item.fecha, true)),
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

  // ========== DISTRIBUCIÓN DE PAGOS ==========
  private cargarDistribucionPagos(): Promise<void> {
    return new Promise((resolve) => {
      this.estadisticaService.obtenerDistribucionPagos(this.fechaInicio, this.fechaFin)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data: any[]) => {
            this.distribucionPagosData = (data || []).map(item => ({ medioPago: item[0] || '', total: item[1] || 0 }));
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
    return new Promise((resolve) => {
      this.estadisticaService.obtenerVentasPorHora(this.fechaInicio)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data: any[]) => {
            this.ventasPorHoraData = (data || []).map(item => ({ hora: item[0] || 0, total: item[1] || 0 }));
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

  // ========== INTERPRETACIÓN AUTOMÁTICA (usada en mini-KPIs y en el PDF) ==========
  private calcularInterpretacion(): void {
    if (this.ventasPorDiaData.length === 0 && this.distribucionPagosData.length === 0 && this.ventasPorHoraData.length === 0) {
      this.resumen = null;
      return;
    }

    let mejorDia = '—', peorDia = '—', mejorDiaTotal = 0, peorDiaTotal = 0, promedioDiario = 0;
    if (this.ventasPorDiaData.length > 0) {
      const ordenado = [...this.ventasPorDiaData].sort((a, b) => b.total - a.total);
      mejorDia = ordenado[0].fecha;
      mejorDiaTotal = ordenado[0].total;
      peorDia = ordenado[ordenado.length - 1].fecha;
      peorDiaTotal = ordenado[ordenado.length - 1].total;
      promedioDiario = this.ventasPorDiaData.reduce((a, b) => a + b.total, 0) / this.ventasPorDiaData.length;
    }

    let horaPico = '—', horaPicoTotal = 0;
    if (this.ventasPorHoraData.length > 0) {
      const ordenadoHoras = [...this.ventasPorHoraData].sort((a, b) => b.total - a.total);
      horaPico = `${ordenadoHoras[0].hora}:00`;
      horaPicoTotal = ordenadoHoras[0].total;
    }

    let medioPagoPrincipal = '—', medioPagoPrincipalPct = 0;
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

  // ========== TEXTOS DE INTERPRETACIÓN (solo se usan dentro del PDF) ==========
  private interpretacionVentasPorDia(): string {
    if (!this.resumen || this.ventasPorDiaData.length === 0) {
      return 'No se registraron ventas en el período seleccionado.';
    }
    return `El día con mayores ventas fue ${this.humanizeDate(this.resumen.mejorDia, true)} (S/ ${this.resumen.mejorDiaTotal.toFixed(2)}). ` +
      `El de menor actividad fue ${this.humanizeDate(this.resumen.peorDia, true)} (S/ ${this.resumen.peorDiaTotal.toFixed(2)}). ` +
      `El promedio diario del período fue S/ ${this.resumen.promedioDiario.toFixed(2)}, y ${this.resumen.tendenciaTexto}.`;
  }

  private interpretacionMediosPago(): string {
    if (!this.resumen || this.distribucionPagosData.length === 0) {
      return 'No se registraron ventas por medio de pago en este período.';
    }
    return `El medio de pago más utilizado fue "${this.resumen.medioPagoPrincipal}", representando el ` +
      `${this.resumen.medioPagoPrincipalPct.toFixed(1)}% del total facturado. Esto sugiere priorizar ese canal ` +
      `en la operación diaria de caja.`;
  }

  private interpretacionVentasPorHora(): string {
    if (!this.resumen || this.ventasPorHoraData.length === 0) {
      return 'No se registraron ventas por hora en la fecha seleccionada.';
    }
    return `La hora de mayor actividad comercial fue a las ${this.resumen.horaPico} horas, con ventas de ` +
      `S/ ${this.resumen.horaPicoTotal.toFixed(2)}. Es un buen dato para planificar turnos de personal.`;
  }

  // ========== EXPORTAR A PDF ==========
  // modo 'actual'  -> usa el período que el usuario tiene visible ahora mismo
  // modo 'hoy'     -> fuerza la carga de datos de HOY, genera el PDF, y restaura el filtro anterior
  async exportarPDF(modo: 'actual' | 'hoy'): Promise<void> {
    if (this.operacionEnCurso) { return; }

    this.exportando = modo;
    this.cdr.detectChanges();

    const filtroPrevio = { inicio: this.fechaInicio, fin: this.fechaFin, activo: this.filtroActivo };
    const esReporteDeHoy = modo === 'hoy';

    try {
      if (esReporteDeHoy) {
        const hoyStr = this.formatDate(new Date());
        if (this.fechaInicio !== hoyStr || this.fechaFin !== hoyStr) {
          this.fechaInicio = hoyStr;
          this.fechaFin = hoyStr;
          this.filtroActivo = 'hoy';
          await this.cargarDatos();
        }
      }

      if (!this.hayDatos) {
        Swal.fire('Sin datos', 'No hay información para generar el reporte en el período seleccionado.', 'info');
        return;
      }

      this.generarDocumentoPDF(esReporteDeHoy);
      this.notificarExito('PDF generado correctamente');
    } catch (err) {
      console.error('Error exportando PDF:', err);
      Swal.fire('Error', 'No se pudo generar el PDF. Intenta nuevamente.', 'error');
    } finally {
      if (esReporteDeHoy && (filtroPrevio.inicio !== this.fechaInicio || filtroPrevio.fin !== this.fechaFin)) {
        this.fechaInicio = filtroPrevio.inicio;
        this.fechaFin = filtroPrevio.fin;
        this.filtroActivo = filtroPrevio.activo;
        await this.cargarDatos();
      }
      this.exportando = null;
      this.cdr.detectChanges();
    }
  }

  /** Dibuja el gráfico + su cuadrito de interpretación al costado, estilo "informe". Devuelve el nuevo Y. */
  private dibujarSeccionGrafico(doc: any, opts: {
    titulo: string;
    imagenBase64: string | undefined;
    interpretacion: string;
    x: number; y: number; anchoImg: number; altoImg: number; anchoBox: number;
  }): number {
    const { titulo, imagenBase64, interpretacion, x, y, anchoImg, altoImg, anchoBox } = opts;

    doc.setFontSize(12);
    doc.setTextColor(COLOR_NAVY);
    doc.text(titulo, x, y);

    if (imagenBase64) {
      doc.setDrawColor('#e2e8f0');
      doc.roundedRect(x, y + 4, anchoImg, altoImg, 3, 3, 'S');
      doc.addImage(imagenBase64, 'PNG', x + 1.5, y + 5.5, anchoImg - 3, altoImg - 3);
    }

    const boxX = x + anchoImg + 8;
    const boxY = y + 4;
    doc.setFillColor('#f0fdfa');
    doc.setDrawColor(COLOR_PRIMARIO);
    doc.roundedRect(boxX, boxY, anchoBox, altoImg, 3, 3, 'FD');

    doc.setFontSize(9.5);
    doc.setTextColor(COLOR_PRIMARIO_OSCURO);
    doc.text('Interpretación', boxX + 5, boxY + 8);

    doc.setFontSize(8.3);
    doc.setTextColor('#334155');
    const lineas = doc.splitTextToSize(interpretacion, anchoBox - 10);
    doc.text(lineas, boxX + 5, boxY + 15);

    return y + altoImg + 14;
  }

  private generarDocumentoPDF(esHoy: boolean): void {
    const doc = new jspdf.jsPDF('landscape', 'mm', 'a4');
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 18;

    // Encabezado
    doc.setFillColor(COLOR_NAVY);
    doc.rect(0, 0, pageWidth, 20, 'F');
    doc.setFontSize(16);
    doc.setTextColor('#ffffff');
    doc.text('ServiFarma · Reporte de Estadísticas', pageWidth / 2, 12, { align: 'center' });

    doc.setFontSize(10);
    doc.setTextColor('#475569');
    const tituloPeriodo = esHoy ? `Reporte del día: ${this.humanizeDate(this.fechaInicio)}` : this.periodoTitulo;
    doc.text(tituloPeriodo, pageWidth / 2, 28, { align: 'center' });
    doc.setFontSize(8);
    doc.text(`Generado el ${this.humanizeDate(this.formatDate(new Date()))}`, pageWidth / 2, 33, { align: 'center' });

    let y = 42;

    // KPIs
    const metrics = [
      { label: 'Total Ventas', value: `S/ ${this.totalVentas.toFixed(2)}` },
      { label: 'Transacciones', value: `${this.totalTransacciones}` },
      { label: 'Ticket Promedio', value: `S/ ${this.ticketPromedio.toFixed(2)}` },
      { label: 'Variación vs. período anterior', value: `${this.variacionPorcentual >= 0 ? '+' : ''}${this.variacionPorcentual.toFixed(1)}%` }
    ];
    metrics.forEach((m, i) => {
      const offsetX = margin + i * 65;
      doc.setFontSize(9);
      doc.setTextColor('#64748b');
      doc.text(m.label, offsetX, y);
      doc.setFontSize(13);
      doc.setTextColor(COLOR_PRIMARIO_OSCURO);
      doc.text(m.value, offsetX, y + 7);
    });

    y += 18;

    const anchoImg = 140;
    const altoImg = 72;
    const anchoBox = pageWidth - margin * 2 - anchoImg - 8;

    const imgBar = this.barChartRef?.chart?.toBase64Image('image/png', 1);
    y = this.dibujarSeccionGrafico(doc, {
      titulo: 'Ventas por Día', imagenBase64: imgBar, interpretacion: this.interpretacionVentasPorDia(),
      x: margin, y, anchoImg, altoImg, anchoBox
    });

    if (y > pageHeight - 90) { doc.addPage(); y = 20; }

    const imgPie = this.pieChartRef?.chart?.toBase64Image('image/png', 1);
    y = this.dibujarSeccionGrafico(doc, {
      titulo: 'Medios de Pago', imagenBase64: imgPie, interpretacion: this.interpretacionMediosPago(),
      x: margin, y, anchoImg, altoImg, anchoBox
    });

    if (y > pageHeight - 90) { doc.addPage(); y = 20; }

    const imgLine = this.lineChartRef?.chart?.toBase64Image('image/png', 1);
    y = this.dibujarSeccionGrafico(doc, {
      titulo: `Ventas por Hora (${this.humanizeDate(this.fechaInicio, true)})`, imagenBase64: imgLine,
      interpretacion: this.interpretacionVentasPorHora(),
      x: margin, y, anchoImg, altoImg, anchoBox
    });

    // Tablas de respaldo
    if (this.ventasPorDiaData.length > 0) {
      if (y > pageHeight - 50) { doc.addPage(); y = 20; }
      doc.setFontSize(11);
      doc.setTextColor(COLOR_NAVY);
      doc.text('Detalle: Ventas por Día', margin, y);
      y += 6;
      jspdfAutotable(doc, {
        head: [['Fecha', 'Total Ventas']],
        body: this.ventasPorDiaData.map(item => [this.humanizeDate(item.fecha, true), `S/ ${item.total.toFixed(2)}`]),
        startY: y, theme: 'striped',
        headStyles: { fillColor: COLOR_PRIMARIO, textColor: '#fff', fontSize: 9 },
        bodyStyles: { fontSize: 8 },
        margin: { left: margin, right: margin }
      });
      y = (doc as any).lastAutoTable.finalY + 10;
    }

    if (this.distribucionPagosData.length > 0) {
      if (y > pageHeight - 50) { doc.addPage(); y = 20; }
      doc.setFontSize(11);
      doc.setTextColor(COLOR_NAVY);
      doc.text('Detalle: Medios de Pago', margin, y);
      y += 6;
      jspdfAutotable(doc, {
        head: [['Medio de Pago', 'Total']],
        body: this.distribucionPagosData.map(item => [item.medioPago, `S/ ${item.total.toFixed(2)}`]),
        startY: y, theme: 'striped',
        headStyles: { fillColor: COLOR_PRIMARIO, textColor: '#fff', fontSize: 9 },
        bodyStyles: { fontSize: 8 },
        margin: { left: margin, right: margin }
      });
      y = (doc as any).lastAutoTable.finalY + 10;
    }

    if (this.ventasPorHoraData.length > 0) {
      if (y > pageHeight - 50) { doc.addPage(); y = 20; }
      doc.setFontSize(11);
      doc.setTextColor(COLOR_NAVY);
      doc.text('Detalle: Ventas por Hora', margin, y);
      y += 6;
      jspdfAutotable(doc, {
        head: [['Hora', 'Total Ventas']],
        body: this.ventasPorHoraData.map(item => [`${item.hora}:00`, `S/ ${item.total.toFixed(2)}`]),
        startY: y, theme: 'striped',
        headStyles: { fillColor: COLOR_PRIMARIO, textColor: '#fff', fontSize: 9 },
        bodyStyles: { fontSize: 8 },
        margin: { left: margin, right: margin }
      });
    }

    const nombreArchivo = esHoy
      ? `Estadisticas_Hoy_${this.fechaInicio}.pdf`
      : `Estadisticas_${this.fechaInicio}_al_${this.fechaFin}.pdf`;
    doc.save(nombreArchivo);
  }

  private notificarExito(mensaje: string): void {
    Swal.fire({
      toast: true, position: 'top-end', icon: 'success', title: mensaje,
      showConfirmButton: false, timer: 2500, timerProgressBar: true
    });
  }
}