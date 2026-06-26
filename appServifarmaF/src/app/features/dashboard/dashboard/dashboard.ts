import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
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
import { DemandaService } from '../../../core/services/demanda';
import { interval, Subscription, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

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
    provideCharts(withDefaultRegisterables()) // <--- Fundamental para que funcione el gráfico
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit, OnDestroy, AfterViewInit {
  // ==============================
  // DATOS DEL USUARIO
  // ==============================
  usuario: string = '';
  rol: string = '';
  isAdmin: boolean = false;
  errorGeneral: string = '';

  // ==============================
  // KPIs (ADMIN) - DATOS REALES
  // Orden fijo desde el inicio para que las 4 tarjetas
  // siempre aparezcan en el mismo lugar, sin importar
  // en qué orden respondan las llamadas a la API.
  // ==============================
  kpis: any[] = [
    { label: 'Ventas del Día', displayValue: 'S/ 0.00', icon: 'bi-cart3', color: '#16a34a', ruta: '/ventas' },
    { label: 'Caja Actual', displayValue: 'S/ 0.00', icon: 'bi-wallet2', color: '#2563eb', ruta: '/caja' },
    { label: 'Productos Críticos', displayValue: '0', icon: 'bi-exclamation-triangle-fill', color: '#dc2626', ruta: '/productos' },
    { label: 'Próximos a Vencer', displayValue: '0', icon: 'bi-archive-fill', color: '#f59e0b', ruta: '/lotes/proximos-a-vencer' }
  ];

  totalVentasHoy: number = 0;
  cajaActual: number = 0;
  productosCriticos: number = 0;
  productosTotales: number = 0;
  proximosVencer: number = 0;
  demandaInsatisfecha: number = 0;

  // ==============================
  // VENTAS RECIENTES (ADMIN)
  // ==============================
  ventasRecientes: any[] = [];
  cargandoVentas: boolean = false;

  // ==============================
  // STOCK CRÍTICO (ADMIN)
  // ==============================
  stockCritico: any[] = [];
  cargandoStock: boolean = false;

  // ==============================
  // ACCESOS RÁPIDOS (VENDEDOR)
  // ==============================
  accesosRapidos = [
    { titulo: 'Buscar Producto', descripcion: 'Búsqueda visual de productos', icon: 'bi-search', color: '#16a34a', ruta: '/productos' },
    { titulo: 'Consultar Stock', descripcion: 'Ver inventario disponible', icon: 'bi-box-seam', color: '#f59e0b', ruta: '/lotes' },
    { titulo: 'Demanda Insatisfecha', descripcion: 'Registrar solicitudes', icon: 'bi-exclamation-diamond-fill', color: '#dc2626', ruta: '/demanda/registrar' },
  ];

  // ==============================
  // ACTIVIDAD RECIENTE (VENDEDOR)
  // ==============================
  actividadReciente: any[] = [];
  cargandoActividad: boolean = false;

  // ==============================
  // ALERTAS (compartidas)
  // ==============================
  alertasStock: any[] = [];
  alertasVencimiento: any[] = [];
  cargandoAlertas: boolean = false;

  // ==============================
  // OTRAS SECCIONES DEL DASHBOARD
  // ==============================
  topProductos: any[] = [];
  inventarioGeneral: any[] = [];
  alertasCriticas: any[] = [];

  // ==============================
  // GRÁFICO: Ventas Mensuales (datos reales)
  // ==============================
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          usePointStyle: true,
          pointStyle: 'circle',
          padding: 20,
          font: { size: 12, weight: '500' },
          color: '#374151'
        }
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
            const label = context.dataset.label || '';
            const value = context.raw as number;
            const formatted = value !== undefined && value !== null
              ? value.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
              : '0.00';
            return `${label}: S/ ${formatted}`;
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: {
          color: 'rgba(0, 0, 0, 0.06)',
        },
        ticks: {
          callback: (value) => `S/ ${value}`,
          font: { size: 11, weight: '400' },
          color: '#6b7280'
        }
      },
      x: {
        grid: { display: false },
        ticks: {
          font: { size: 10, weight: '400' },
          color: '#6b7280',
          maxRotation: 45,
          minRotation: 30
        }
      }
    },
    animation: {
      duration: 800,
      easing: 'easeInOutQuart'
    }
  };

  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Ventas (S/)',
        backgroundColor: '#0d6efd',
        barPercentage: 0.8,
        categoryPercentage: 0.8
      }
    ]
  };

  // ==============================
  // ESTADO DE CARGA GENERAL
  // ==============================
  cargando: boolean = true;
  cargandoGrafico: boolean = false;

  // ==============================
  // POLLING (ACTUALIZACIÓN AUTOMÁTICA)
  // ==============================
  private pollingSubscription?: Subscription;
  private readonly POLLING_INTERVAL = 60000; // 60 segundos

  constructor(
    private authService: AuthService,
    private ventaService: VentaService,
    private cajaService: CajaService,
    private productoService: ProductoService,
    private estadisticaService: EstadisticaService,
    private loteService: LoteService,
    private demandaService: DemandaService
  ) { }

  ngOnInit(): void {
    this.usuario = this.authService.getUsuario() || 'Usuario';
    this.rol = this.authService.getRol() || '';
    this.isAdmin = this.rol.toUpperCase() === 'ADMIN';

    this.cargarTodosLosDatos();
    this.iniciarPolling();
  }

  ngAfterViewInit(): void {
    // Forzar actualización del gráfico después de que la vista esté lista
    setTimeout(() => {
      if (this.barChartData.labels?.length === 0) {
        this.cargarGrafico();
      }
    }, 500);
  }

  ngOnDestroy(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  // ==============================
  // CARGA COMPLETA DE DATOS
  // ==============================
  cargarTodosLosDatos(): void {
    this.cargando = true;
    this.errorGeneral = '';

    if (this.isAdmin) {
      this.cargarKpisAdmin();
      this.cargarVentasRecientes();
      this.cargarStockCritico();
      this.cargarTopProductos();
      this.cargarInventarioGeneral();
      this.cargarAlertasCriticas();
      this.cargarGrafico();
    } else {
      this.cargarActividadReciente();
    }

    this.cargarAlertas();

    setTimeout(() => {
      this.cargando = false;
    }, 1500);
  }

  refrescarDatos(): void {
    this.cargarTodosLosDatos();
  }

  private iniciarPolling(): void {
    this.pollingSubscription = interval(this.POLLING_INTERVAL)
      .subscribe(() => {
        this.cargarTodosLosDatos();
      });
  }

  // ==============================
  // KPIs PARA ADMIN (DATOS REALES)
  // ==============================
  private cargarKpisAdmin(): void {
    const hoy = new Date();
    const inicio = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate(), 0, 0, 0);
    const fin = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate(), 23, 59, 59);
    const inicioStr = inicio.toISOString();
    const finStr = fin.toISOString();

    // 1. Ventas del día
    this.estadisticaService.obtenerTotalVentas(inicioStr, finStr).subscribe({
      next: (total) => {
        this.totalVentasHoy = total || 0;
        this.actualizarKpi('Ventas del Día', `S/ ${this.totalVentasHoy.toFixed(2)}`);
      },
      error: () => this.actualizarKpi('Ventas del Día', 'S/ 0.00')
    });

    // 2. Caja actual (usando el total de ventas de la caja abierta)
    this.cajaService.obtenerCajaAbierta().subscribe({
      next: (caja) => {
        if (caja && caja.id) {
          this.cajaService.obtenerTotalVentas(caja.id).subscribe({
            next: (total) => {
              this.cajaActual = total || 0;
              this.actualizarKpi('Caja Actual', `S/ ${this.cajaActual.toFixed(2)}`);
            },
            error: () => this.actualizarKpi('Caja Actual', 'S/ 0.00')
          });
        } else {
          this.cajaActual = 0;
          this.actualizarKpi('Caja Actual', 'S/ 0.00');
        }
      },
      error: () => {
        this.cajaActual = 0;
        this.actualizarKpi('Caja Actual', 'S/ 0.00');
      }
    });

    // 3. Productos críticos (stock bajo)
    this.productoService.obtenerProductosConStockBajo().subscribe({
      next: (productos) => {
        this.productosCriticos = productos?.length || 0;
        this.actualizarKpi('Productos Críticos', `${this.productosCriticos}`);
      },
      error: () => this.actualizarKpi('Productos Críticos', '0')
    });

    // 4. Productos totales (no se muestra en las 4 tarjetas principales,
    // pero queda calculado por si se usa en otra sección)
    this.productoService.listar().subscribe({
      next: (productos) => {
        this.productosTotales = productos?.length || 0;
        this.actualizarKpi('Productos Totales', `${this.productosTotales}`);
      },
      error: () => this.actualizarKpi('Productos Totales', '0')
    });

    // 5. Próximos a vencer (lotes con vencimiento en 30 días)
    this.loteService.obtenerProximosAVencer(30).subscribe({
      next: (lotes) => {
        this.proximosVencer = lotes?.length || 0;
        this.actualizarKpi('Próximos a Vencer', `${this.proximosVencer}`);
      },
      error: () => this.actualizarKpi('Próximos a Vencer', '0')
    });

    // 6. Demanda insatisfecha (no se muestra en las 4 tarjetas principales,
    // pero queda calculado por si se usa en otra sección)
    this.demandaService.listar().subscribe({
      next: (demandas) => {
        this.demandaInsatisfecha = demandas?.length || 0;
        this.actualizarKpi('Demanda Insatisfecha', `${this.demandaInsatisfecha}`);
      },
      error: () => this.actualizarKpi('Demanda Insatisfecha', '0')
    });
  }

  private actualizarKpi(label: string, valor: string): void {
    const kpi = this.kpis.find(k => k.label === label);
    if (kpi) {
      kpi.displayValue = valor;
    } else {
      // KPIs adicionales que no forman parte de las 4 tarjetas principales
      // (Productos Totales, Demanda Insatisfecha) se agregan al final
      // sin afectar el orden fijo de las primeras 4.
      const iconos: any = {
        'Productos Totales': 'bi-boxes',
        'Demanda Insatisfecha': 'bi-graph-down-arrow'
      };
      const colores: any = {
        'Productos Totales': '#7c3aed',
        'Demanda Insatisfecha': '#ea580c'
      };
      this.kpis.push({
        label,
        displayValue: valor,
        icon: iconos[label] || 'bi-circle',
        color: colores[label] || '#6b7280',
        ruta: '#'
      });
    }
  }

  // ==============================
  // VENTAS RECIENTES (REALES)
  // ==============================
  private cargarVentasRecientes(): void {
    this.cargandoVentas = true;
    this.ventaService.obtenerUltimas(10).subscribe({
      next: (ventas) => {
        this.ventasRecientes = ventas?.map(v => ({
          cliente: v.clienteNombre || 'Anónimo',
          productos: v.detalles?.length || 0,
          hora: new Date(v.fecha).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' }),
          total: v.total || 0
        })) || [];
        this.cargandoVentas = false;
      },
      error: () => {
        this.ventasRecientes = [];
        this.cargandoVentas = false;
      }
    });
  }

  // ==============================
  // STOCK CRÍTICO (REAL)
  // ==============================
  private cargarStockCritico(): void {
    this.cargandoStock = true;
    this.productoService.obtenerProductosConStockBajo().subscribe({
      next: (productos) => {
        this.stockCritico = productos?.map(p => ({
          producto: p.nombre,
          stock: p.stockMinimo || 0,
          minimo: p.stockMinimo || 5
        })) || [];
        this.cargandoStock = false;
      },
      error: () => {
        this.stockCritico = [];
        this.cargandoStock = false;
      }
    });
  }

  // ==============================
  // TOP PRODUCTOS POR ROTACIÓN (REAL)
  // ==============================
  private cargarTopProductos(): void {
    this.ventaService.obtenerUltimas(200).pipe(
      catchError(() => of([]))
    ).subscribe({
      next: (ventas) => {
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
          .slice(0, 10);
      },
      error: () => {
        this.topProductos = [];
      }
    });
  }

  // ==============================
  // INVENTARIO GENERAL (REAL)
  // ==============================
  private cargarInventarioGeneral(): void {
    this.productoService.listar().subscribe({
      next: (productos) => {
        this.inventarioGeneral = productos.map(p => ({
          ...p,
          stockActual: 0, // Se actualiza abajo con los lotes
          ubicacion: 'N/A',
          ultimaCompra: 'N/A'
        }));
        // Cargar stock real desde lotes (sumar cantidades)
        this.loteService.listar().subscribe({
          next: (lotes) => {
            const stockMap = new Map<number, number>();
            lotes.forEach(l => {
              if (l.estado === 'activo') {
                const current = stockMap.get(l.productoId) || 0;
                stockMap.set(l.productoId, current + l.cantidad);
              }
            });
            this.inventarioGeneral = this.inventarioGeneral.map(p => ({
              ...p,
              stockActual: stockMap.get(p.id) || 0
            }));
          },
          error: () => {
            // Si falla, dejar stock en 0
          }
        });
      },
      error: () => {
        this.inventarioGeneral = [];
      }
    });
  }

  // ==============================
  // ALERTAS CRÍTICAS (REAL)
  // ==============================
  private cargarAlertasCriticas(): void {
    const alertas: any[] = [];

    this.productoService.obtenerProductosConStockBajo().subscribe({
      next: (productos) => {
        productos.forEach(p => {
          alertas.push({
            tipo: 'stock',
            titulo: `Stock Crítico: ${p.nombre}`,
            descripcion: `Stock actual: 0 unidades (mínimo requerido: ${p.stockMinimo})`,
            fecha: new Date().toLocaleDateString('es-PE')
          });
        });
        this.alertasCriticas = alertas.slice(0, 8);
      },
      error: () => { }
    });

    this.loteService.obtenerProximosAVencer(30).subscribe({
      next: (lotes) => {
        lotes.forEach(l => {
          alertas.push({
            tipo: 'vencimiento',
            titulo: `Próximo a Vencer: ${l.productoNombre || 'Producto'}`,
            descripcion: `Lote ${l.lote} - Vence en ${this.calcularDiasRestantes(l.fechaVencimiento)} días`,
            fecha: new Date(l.fechaVencimiento).toLocaleDateString('es-PE')
          });
        });
        this.alertasCriticas = alertas.slice(0, 8);
      },
      error: () => { }
    });
  }

  // ==============================
  // GRÁFICO (DATOS REALES)
  // ==============================
  private cargarGrafico(): void {
    this.cargandoGrafico = true;
    this.ventaService.listar().subscribe({
      next: (ventas) => {
        const hoy = new Date();
        const mesesMap = new Map<string, number>();
        const labelsPorKey = new Map<string, string>();
        const ordenKeys: string[] = [];

        // Inicializar los últimos 12 meses en 0
        for (let i = 11; i >= 0; i--) {
          const fecha = new Date(hoy.getFullYear(), hoy.getMonth() - i, 1);
          const key = `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}`;
          const label = fecha.toLocaleDateString('es-PE', { month: 'short', year: 'numeric' });
          mesesMap.set(key, 0);
          labelsPorKey.set(key, label);
          ordenKeys.push(key);
        }

        // Sumar ventas reales por mes
        ventas.forEach(v => {
          const fecha = new Date(v.fecha);
          const key = `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}`;
          if (mesesMap.has(key)) {
            mesesMap.set(key, (mesesMap.get(key) || 0) + (v.total || 0));
          }
        });

        this.barChartData.labels = ordenKeys.map(key => labelsPorKey.get(key) || key);
        this.barChartData.datasets[0].data = ordenKeys.map(key => mesesMap.get(key) || 0);
        this.cargandoGrafico = false;
      },
      error: () => {
        this.cargandoGrafico = false;
      }
    });
  }

  // ==============================
  // ACTIVIDAD RECIENTE (VENDEDOR)
  // ==============================
  private cargarActividadReciente(): void {
    this.cargandoActividad = true;
    this.ventaService.obtenerUltimas(10).subscribe({
      next: (ventas) => {
        this.actividadReciente = ventas?.map(v => ({
          descripcion: 'Venta registrada',
          cliente: v.clienteNombre || 'Anónimo',
          hora: new Date(v.fecha).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' }),
          monto: v.total || 0
        })) || [];
        this.cargandoActividad = false;
      },
      error: () => {
        this.actividadReciente = [];
        this.cargandoActividad = false;
      }
    });
  }

  // ==============================
  // ALERTAS (REALES)
  // ==============================
  private cargarAlertas(): void {
    this.cargandoAlertas = true;

    this.productoService.obtenerProductosConStockBajo().subscribe({
      next: (productos) => {
        this.alertasStock = productos?.map(p => ({
          producto: p.nombre,
          stock: p.stockMinimo || 0
        })) || [];
        this.cargandoAlertas = false;
      },
      error: () => {
        this.alertasStock = [];
        this.cargandoAlertas = false;
      }
    });

    this.loteService.obtenerProximosAVencer(30).subscribe({
      next: (lotes) => {
        this.alertasVencimiento = lotes?.map(l => ({
          producto: l.productoNombre || `Producto #${l.productoId}`,
          dias: this.calcularDiasRestantes(l.fechaVencimiento)
        })) || [];
        this.alertasVencimiento.sort((a, b) => a.dias - b.dias);
        if (this.alertasVencimiento.length > 5) {
          this.alertasVencimiento = this.alertasVencimiento.slice(0, 5);
        }
        this.cargandoAlertas = false;
      },
      error: () => {
        this.alertasVencimiento = [];
        this.cargandoAlertas = false;
      }
    });
  }

  private calcularDiasRestantes(fechaVencimiento: string): number {
    const hoy = new Date();
    const vencimiento = new Date(fechaVencimiento);
    const diffTime = vencimiento.getTime() - hoy.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays > 0 ? diffDays : 0;
  }
}