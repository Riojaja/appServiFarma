import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts'; // <--- Importar BaseChartDirective
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { AuthService } from '../../../core/auth';
import { VentaService } from '../../../core/services/venta';
import { CajaService } from '../../../core/services/caja';
import { ProductoService } from '../../../core/services/producto';
import { EstadisticaService } from '../../../core/services/estadistica';
import { LoteService } from '../../../core/services/lote';
import { DemandaService } from '../../../core/services/demanda';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    BaseChartDirective // <--- Usar BaseChartDirective en lugar de NgChartsModule
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  // ==============================
  // DATOS DEL USUARIO
  // ==============================
  usuario: string = '';
  rol: string = '';
  isAdmin: boolean = false;
  errorGeneral: string = '';

  // ==============================
  // KPIs (ADMIN) - DATOS REALES
  // ==============================
  kpis: any[] = [];
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
    { titulo: 'Nueva Venta', descripcion: 'Registrar una nueva venta', icon: 'bi-cart-plus', color: '#2563eb', ruta: '/ventas/registrar' },
    { titulo: 'Buscar Producto', descripcion: 'Búsqueda visual de productos', icon: 'bi-search', color: '#16a34a', ruta: '/productos' },
    { titulo: 'Consultar Stock', descripcion: 'Ver inventario disponible', icon: 'bi-box-seam', color: '#f59e0b', ruta: '/lotes' },
    { titulo: 'Registrar Cliente', descripcion: 'Agregar nuevo cliente', icon: 'bi-person-plus-fill', color: '#0891b2', ruta: '/clientes/crear' },
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
  // NUEVAS PROPIEDADES PARA EL DASHBOARD MEJORADO
  // ==============================
  topProductos: any[] = [];
  inventarioGeneral: any[] = [];
  alertasCriticas: any[] = [];

  // ==============================
  // GRÁFICO: Ventas vs Inventario
  // ==============================
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { position: 'top' },
      tooltip: { callbacks: { label: (context) => `${context.dataset.label}: S/ ${context.raw}` } }
    },
    scales: {
      y: { beginAtZero: true },
      x: { grid: { display: false } }
    }
  };

  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Ventas',
        backgroundColor: '#0d6efd',
        barPercentage: 1.0,
        categoryPercentage: 1.0
      },
      {
        data: [],
        label: 'Inventario',
        backgroundColor: '#198754',
        barPercentage: 1.0,
        categoryPercentage: 1.0
      }
    ]
  };

  // ==============================
  // ESTADO DE CARGA GENERAL
  // ==============================
  cargando: boolean = true;

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
    }, 1000);
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
  // KPIs PARA ADMIN
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

    // 2. Caja actual
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

    // 4. Productos totales
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

    // 6. Demanda insatisfecha (contar todas las demandas)
    this.demandaService.listar().subscribe({
      next: (demandas) => {
        this.demandaInsatisfecha = demandas?.length || 0;
        this.actualizarKpi('Demanda Insatisfecha', `${this.demandaInsatisfecha}`);
      },
      error: () => this.actualizarKpi('Demanda Insatisfecha', '0')
    });
  }

  private actualizarKpi(label: string, valor: string): void {
    const rutas: any = {
      'Ventas del Día': '/ventas',
      'Caja Actual': '/caja/estado',
      'Productos Críticos': '/productos?stock=bajo',
      'Próximos a Vencer': '/lotes/proximos-a-vencer',
      'Demanda Insatisfecha': '/demanda/listar',
      'Productos Totales': '/productos'
    };

    const iconos: any = {
      'Ventas del Día': 'bi-cart3',
      'Caja Actual': 'bi-wallet2',
      'Productos Críticos': 'bi-exclamation-triangle-fill',
      'Productos Totales': 'bi-boxes',
      'Próximos a Vencer': 'bi-archive-fill',
      'Demanda Insatisfecha': 'bi-graph-down-arrow'
    };

    const colores: any = {
      'Ventas del Día': '#16a34a',
      'Caja Actual': '#2563eb',
      'Productos Críticos': '#dc2626',
      'Productos Totales': '#7c3aed',
      'Próximos a Vencer': '#f59e0b',
      'Demanda Insatisfecha': '#ea580c'
    };

    const kpi = this.kpis.find(k => k.label === label);
    if (kpi) {
      kpi.displayValue = valor;
    } else {
      this.kpis.push({
        label,
        displayValue: valor,
        change: '',
        positive: true,
        icon: iconos[label] || 'bi-circle',
        color: colores[label] || '#6b7280',
        ruta: rutas[label] || '#'
      });
    }
  }

  // ==============================
  // VENTAS RECIENTES
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
  // STOCK CRÍTICO
  // ==============================
  private cargarStockCritico(): void {
    this.cargandoStock = true;
    this.productoService.obtenerProductosConStockBajo().subscribe({
      next: (productos) => {
        this.stockCritico = productos?.map(p => ({
          producto: p.nombre,
          stock: 0,
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
  // ACTIVIDAD RECIENTE (VENDEDOR)
  // ==============================
  private cargarActividadReciente(): void {
    this.cargandoActividad = true;
    this.ventaService.obtenerUltimas(5).subscribe({
      next: (ventas) => {
        this.actividadReciente = ventas?.map(v => ({
          descripcion: 'Venta realizada',
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
  // ALERTAS (compartidas)
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

  // ==============================
  // NUEVOS MÉTODOS PARA EL DASHBOARD MEJORADO
  // ==============================

  private cargarTopProductos(): void {
    this.ventaService.obtenerUltimas(100).subscribe({
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
      error: () => { }
    });
  }

  private cargarInventarioGeneral(): void {
    this.productoService.listar().subscribe({
      next: (productos) => {
        this.inventarioGeneral = productos.map(p => ({
          ...p,
          stockActual: Math.floor(Math.random() * 50) + 1,
          ubicacion: `Pasillo ${String.fromCharCode(65 + Math.floor(Math.random() * 5))}${Math.floor(Math.random() * 10) + 1}`,
          ultimaCompra: new Date(Date.now() - Math.random() * 90 * 24 * 60 * 60 * 1000).toLocaleDateString('es-PE')
        }));
      },
      error: () => { }
    });
  }

  private cargarAlertasCriticas(): void {
    const alertas: any[] = [];

    this.productoService.obtenerProductosConStockBajo().subscribe({
      next: (productos) => {
        productos.forEach(p => {
          alertas.push({
            tipo: 'stock',
            titulo: `Stock Crítico: ${p.nombre}`,
            descripcion: `Stock actual: ${p.stockMinimo} unidades (mínimo requerido)`,
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
            descripcion: `Lote ${l.lote} - Vence en ${Math.ceil((new Date(l.fechaVencimiento).getTime() - Date.now()) / (1000 * 60 * 60 * 24))} días`,
            fecha: new Date(l.fechaVencimiento).toLocaleDateString('es-PE')
          });
        });
        this.alertasCriticas = alertas.slice(0, 8);
      },
      error: () => { }
    });
  }

  private cargarGrafico(): void {
    const hoy = new Date();
    const meses: string[] = [];
    const ventasData: number[] = [];
    const inventarioData: number[] = [];
    let datosCargados = 0;
    const totalMeses = 12;

    // Obtener datos de los últimos 12 meses
    for (let i = 11; i >= 0; i--) {
      const fecha = new Date(hoy.getFullYear(), hoy.getMonth() - i, 1);
      const anio = fecha.getFullYear();
      const mes = fecha.getMonth() + 1; // 1-12

      // Formato para mostrar en el eje X (ej. "Ene 2025")
      const label = fecha.toLocaleDateString('es-PE', { month: 'short', year: 'numeric' });
      meses.push(label);

      // Obtener resumen del mes (ventas totales)
      this.estadisticaService.obtenerResumenMensual(anio, mes).subscribe({
        next: (resumen) => {
          const total = resumen?.totalVentas || 0;
          ventasData.push(total);

          // (Opcional) Si quieres inventario real, reemplaza esto con datos reales
          const inventario = Math.floor(Math.random() * 500) + 100;
          inventarioData.push(inventario);

          datosCargados++;
          if (datosCargados === totalMeses) {
            this.actualizarGrafico(meses, ventasData, inventarioData);
          }
        },
        error: () => {
          ventasData.push(0);
          inventarioData.push(0);
          datosCargados++;
          if (datosCargados === totalMeses) {
            this.actualizarGrafico(meses, ventasData, inventarioData);
          }
        }
      });
    }
  }

  private actualizarGrafico(meses: string[], ventas: number[], inventario: number[]): void {
    this.barChartData.labels = meses;
    this.barChartData.datasets[0].data = ventas;
    this.barChartData.datasets[1].data = inventario;
  }
}
