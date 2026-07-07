import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { AuthService } from '../../../core/auth';
import { VentaService } from '../../../core/services/venta';
import { CajaService } from '../../../core/services/caja';
import { ProductoService } from '../../../core/services/producto';
import { EstadisticaService } from '../../../core/services/estadistica';
import { LoteService } from '../../../core/services/lote';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    BaseChartDirective
  ],
  providers: [
    provideCharts(withDefaultRegisterables())
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  // DATOS DEL USUARIO
  usuario: string = '';
  rol: string = '';
  isAdmin: boolean = false;
  errorGeneral: string = '';

  // KPIs
  kpis: any[] = [
    { label: 'Ventas del Día', displayValue: 'S/ 0.00', icon: 'bi-cart3', color: '#16a34a', ruta: '/ventas' },
    { label: 'Caja Actual', displayValue: 'S/ 0.00', icon: 'bi-wallet2', color: '#2563eb', ruta: '/caja' },
    { label: 'Productos Críticos', displayValue: '0', icon: 'bi-exclamation-triangle-fill', color: '#dc2626', ruta: '/productos' },
    { label: 'Próximos a Vencer', displayValue: '0', icon: 'bi-archive-fill', color: '#f59e0b', ruta: '/lotes/proximos-a-vencer' }
  ];

  // VENTAS RECIENTES (todas, scroll en CSS)
  ventasRecientes: any[] = [];

  // STOCK CRÍTICO
  stockCritico: any[] = [];

  // TOP PRODUCTOS
  topProductos: any[] = [];

  // ALERTAS LATERALES (compartidas para ambos roles)
  alertasStock: any[] = [];
  alertasVencimiento: any[] = [];

  // ACTIVIDAD RECIENTE (VENDEDOR)
  actividadReciente: any[] = [];

  // GRÁFICO
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: { usePointStyle: true, pointStyle: 'circle', padding: 20, font: { size: 12 } }
      },
      tooltip: {
        backgroundColor: 'rgba(255,255,255,0.95)',
        titleColor: '#1f2937',
        bodyColor: '#374151',
        borderColor: '#e5e7eb',
        borderWidth: 1,
        cornerRadius: 8,
        padding: 12,
        callbacks: {
          label: (context) => {
            const value = context.raw as number;
            return `S/ ${value.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
          }
        }
      }
    },
    scales: {
      y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.06)' }, ticks: { callback: (value) => `S/ ${value}` } },
      x: { grid: { display: false } }
    },
    animation: { duration: 600 }
  };

  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [{ data: [], label: 'Ventas (S/)', backgroundColor: '#0d6efd' }]
  };

  cargando: boolean = true;

  constructor(
    private authService: AuthService,
    private ventaService: VentaService,
    private cajaService: CajaService,
    private productoService: ProductoService,
    private estadisticaService: EstadisticaService,
    private loteService: LoteService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.usuario = this.authService.getUsuario() || 'Usuario';
    this.rol = this.authService.getRol() || '';
    this.isAdmin = this.rol.toUpperCase() === 'ADMIN';

    if (this.isAdmin) {
      this.cargarDatosAdmin();
    } else if (this.rol) {
      this.cargarDatosVendedor();
    } else {
      setTimeout(() => {
        this.rol = this.authService.getRol() || '';
        this.isAdmin = this.rol.toUpperCase() === 'ADMIN';
        if (this.isAdmin) {
          this.cargarDatosAdmin();
        } else if (this.rol) {
          this.cargarDatosVendedor();
        } else {
          this.errorGeneral = 'No se pudo obtener el rol del usuario.';
          this.cargando = false;
          this.cdr.detectChanges();
        }
      }, 100);
    }
  }

  ngOnDestroy(): void { }

  private cargarDatosAdmin(): void {
    this.cargando = true;
    this.errorGeneral = '';

    const hoy = new Date();
    const inicio = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate(), 0, 0, 0);
    const fin = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate(), 23, 59, 59);
    const inicioStr = inicio.toISOString();
    const finStr = fin.toISOString();

    forkJoin({
      ventasHoy: this.estadisticaService.obtenerTotalVentas(inicioStr, finStr).pipe(catchError(() => of(0))),
      cajaAbierta: this.cajaService.obtenerCajaAbierta().pipe(catchError(() => of(null))),
      productosCriticos: this.productoService.obtenerProductosConStockBajo().pipe(catchError(() => of([]))),
      proximosVencer: this.loteService.obtenerProximosAVencer(30).pipe(catchError(() => of([]))),
      ventasRecientes: this.ventaService.obtenerUltimas(20).pipe(catchError(() => of([]))),
      topVentas: this.ventaService.obtenerUltimas(200).pipe(catchError(() => of([]))),
      datosGrafico: this.ventaService.listar().pipe(catchError(() => of([])))
    }).pipe(
      finalize(() => {
        this.cargando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (resultados) => {
        // KPIs
        const ventasHoy = resultados.ventasHoy || 0;
        this.kpis[0].displayValue = `S/ ${ventasHoy.toFixed(2)}`;

        const caja = resultados.cajaAbierta;
        if (caja && caja.id) {
          this.cajaService.obtenerTotalVentas(caja.id).pipe(catchError(() => of(0))).subscribe(total => {
            this.kpis[1].displayValue = `S/ ${(total || 0).toFixed(2)}`;
            this.cdr.detectChanges();
          });
        } else {
          this.kpis[1].displayValue = 'S/ 0.00';
        }

        const criticos = resultados.productosCriticos || [];
        this.kpis[2].displayValue = criticos.length.toString();

        const proximos = resultados.proximosVencer || [];
        this.kpis[3].displayValue = proximos.length.toString();

        // Ventas recientes
        this.ventasRecientes = (resultados.ventasRecientes || []).map(v => ({
          cliente: v.clienteNombre || 'Anónimo',
          productos: v.detalles?.length || 0,
          hora: new Date(v.fecha).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' }),
          total: v.total || 0
        }));

        // Stock crítico
        this.stockCritico = criticos.map(p => ({
          producto: p.nombre,
          stock: p.stockMinimo || 0,
          minimo: p.stockMinimo || 5
        }));

        // Top productos
        const ventas = resultados.topVentas || [];
        const mapa = new Map<string, number>();
        ventas.forEach(v => {
          v.detalles?.forEach(d => {
            const nombre = d.productoNombre || 'Producto';
            mapa.set(nombre, (mapa.get(nombre) || 0) + d.cantidad);
          });
        });
        this.topProductos = Array.from(mapa.entries())
          .map(([nombre, cantidad]) => ({ nombre, cantidad }))
          .sort((a, b) => b.cantidad - a.cantidad)
          .slice(0, 5);

        // Gráfico
        const todasVentas = resultados.datosGrafico || [];
        this.armarDatosGrafico(todasVentas);

        // Alertas laterales (mismo helper que usa el vendedor)
        this.mapearAlertasLaterales(criticos, proximos);

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando dashboard:', err);
        this.errorGeneral = 'Error al cargar los datos. Intente de nuevo.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Antes este método estaba prácticamente vacío (solo apagaba el spinner),
   * por eso "Actividad Reciente" y el panel lateral de "Alertas" (Stock Mínimo
   * y Próximos a Vencer) nunca se llenaban para el rol vendedor, aunque el
   * HTML los muestra igual que para el admin.
   */
  private cargarDatosVendedor(): void {
    this.cargando = true;
    this.errorGeneral = '';

    forkJoin({
      ventasRecientes: this.ventaService.obtenerUltimas(20).pipe(catchError(() => of([]))), // antes: 10
      productosCriticos: this.productoService.obtenerProductosConStockBajo().pipe(catchError(() => of([]))),
      proximosVencer: this.loteService.obtenerProximosAVencer(30).pipe(catchError(() => of([])))
    }).pipe(
      finalize(() => {
        this.cargando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (resultados) => {
        this.actividadReciente = (resultados.ventasRecientes || []).map(v => ({
          descripcion: 'Venta registrada',
          cliente: v.clienteNombre || 'Anónimo',
          hora: new Date(v.fecha).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' }),
          monto: v.total || 0
        }));

        this.mapearAlertasLaterales(resultados.productosCriticos || [], resultados.proximosVencer || []);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando dashboard del vendedor:', err);
        this.errorGeneral = 'Error al cargar los datos. Intente de nuevo.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  /** Lógica compartida por admin y vendedor para llenar el panel lateral de alertas. */
  private mapearAlertasLaterales(criticos: any[], proximos: any[]): void {
    this.alertasStock = criticos.map(p => ({ producto: p.nombre, stock: p.stockMinimo || 0 }));
    this.alertasVencimiento = proximos
      .map(l => ({
        producto: l.productoNombre || `Producto #${l.productoId}`,
        dias: this.calcularDiasRestantes(l.fechaVencimiento)
      }))
      .sort((a, b) => a.dias - b.dias)
      .slice(0, 5);
  }

  private armarDatosGrafico(todasVentas: any[]): void {
    const hoyDate = new Date();
    const mesesMap = new Map<string, number>();
    const labelsPorKey = new Map<string, string>();
    const ordenKeys: string[] = [];

    for (let i = 11; i >= 0; i--) {
      const fecha = new Date(hoyDate.getFullYear(), hoyDate.getMonth() - i, 1);
      const key = `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}`;
      const label = fecha.toLocaleDateString('es-PE', { month: 'short', year: 'numeric' });
      mesesMap.set(key, 0);
      labelsPorKey.set(key, label);
      ordenKeys.push(key);
    }

    todasVentas.forEach(v => {
      const fecha = new Date(v.fecha);
      const key = `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}`;
      if (mesesMap.has(key)) {
        mesesMap.set(key, (mesesMap.get(key) || 0) + (v.total || 0));
      }
    });

    this.barChartData.labels = ordenKeys.map(key => labelsPorKey.get(key) || key);
    this.barChartData.datasets[0].data = ordenKeys.map(key => mesesMap.get(key) || 0);
  }

  refrescarDatos(): void {
    if (this.isAdmin) {
      this.cargarDatosAdmin();
    } else {
      this.cargarDatosVendedor();
    }
  }

  private calcularDiasRestantes(fechaVencimiento: string): number {
    const hoy = new Date();
    const vencimiento = new Date(fechaVencimiento);
    const diffTime = vencimiento.getTime() - hoy.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays > 0 ? diffDays : 0;
  }
}