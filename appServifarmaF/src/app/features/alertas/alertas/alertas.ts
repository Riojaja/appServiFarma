import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { InventarioService } from '../../../core/services/inventario';
import { CategoriaService } from '../../../core/services/categoria';
import { ProductoService } from '../../../core/services/producto';
import { Alerta } from '../../../core/models/alerta.model';
import { FormsModule } from '@angular/forms';

interface ResumenInventario {
  totalProductosStockBajo: number;
  totalProductosSinStock: number;
  totalLotesProximosAVencer: number;
  totalLotesActivosConStock: number;
}

@Component({
  selector: 'app-alertas',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule
  ],
  templateUrl: './alertas.html',
  styleUrl: './alertas.css',
})
export class Alertas implements OnInit {
  // ======== DATOS ========
  alertasStockBajo: Alerta[] = [];
  alertasProximosVencer: Alerta[] = [];
  resumen: ResumenInventario | null = null;

  // ======== MAPA DE CATEGORÍAS POR PRODUCTO ========
  private categoriasPorProducto: Map<number, string> = new Map();

  // ======== FILTROS ========
  filtroActivo: 'todas' | 'stock_bajo' | 'proximo_vencer' = 'todas';
  filtroTexto: string = '';
  filtroEstado: string = '';
  filtroCategoria: string = '';
  categorias: string[] = [];

  // ======== LISTA VISIBLE ========
  alertasVisibles: Alerta[] = [];

  // ======== PAGINACIÓN ========
  paginaActual: number = 1;
  registrosPorPagina: number = 5;
  Math = Math;

  // ======== ESTADOS ========
  cargando = true;
  error = false;

  constructor(
    private inventarioService: InventarioService,
    private categoriaService: CategoriaService,
    private productoService: ProductoService, // <--- Inyectar
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.filtroActivo = 'todas';
    this.cargarCategorias();
    this.cargarProductosYCategorias(); // <--- Cargar productos para mapear categorías
    this.cargarAlertas();
  }

  // ======== CARGAR CATEGORÍAS (para el select) ========
  cargarCategorias(): void {
    this.categoriaService.listar().subscribe({
      next: (cats) => {
        this.categorias = cats.map(c => c.nombre);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Error al cargar categorías:', err);
        this.categorias = [];
      }
    });
  }

  // ======== CARGAR PRODUCTOS Y MAPEAR CATEGORÍAS ========
  cargarProductosYCategorias(): void {
    this.productoService.listar().subscribe({
      next: (productos) => {
        productos.forEach(p => {
          if (p.categoriaNombre) {
            this.categoriasPorProducto.set(p.id!, p.categoriaNombre);
          }
        });
        console.log('📦 Mapa de categorías por producto:', this.categoriasPorProducto);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Error al cargar productos:', err);
      }
    });
  }

  // ======== CARGA DE ALERTAS ========
  cargarAlertas(): void {
    this.cargando = true;
    this.error = false;

    forkJoin({
      stockBajo: this.inventarioService.obtenerStockBajo(),
      proximosVencer: this.inventarioService.obtenerProximosVencer(),
      resumen: this.inventarioService.obtenerResumen()
    }).subscribe({
      next: ({ stockBajo, proximosVencer, resumen }) => {
        console.log('📦 Stock Bajo:', stockBajo);
        console.log('⏳ Próximos a Vencer:', proximosVencer);
        console.log('📊 Resumen:', resumen);

        // 🔥 ENRIQUECER ALERTAS CON CATEGORÍA
        this.alertasStockBajo = this.enriquecerAlertasConCategoria(stockBajo || []);
        this.alertasProximosVencer = this.enriquecerAlertasConCategoria(proximosVencer || []);
        this.resumen = resumen;

        this.actualizarAlertasVisibles();

        setTimeout(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        }, 0);
      },
      error: (err) => {
        console.error('❌ Error al cargar alertas:', err);
        this.error = true;
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ======== ENRIQUECER ALERTAS CON CATEGORÍA ========
  private enriquecerAlertasConCategoria(alertas: Alerta[]): Alerta[] {
    return alertas.map(alerta => {
      const categoria = this.categoriasPorProducto.get(alerta.productoId);
      return {
        ...alerta,
        categoriaNombre: categoria || 'Sin categoría'
      };
    });
  }

  // ======== ACTUALIZAR LISTA VISIBLE ========
  private actualizarAlertasVisibles(): void {
    let base: Alerta[] = [];
    if (this.filtroActivo === 'stock_bajo') {
      base = [...this.alertasStockBajo];
    } else if (this.filtroActivo === 'proximo_vencer') {
      base = [...this.alertasProximosVencer];
    } else {
      base = [...this.alertasProximosVencer, ...this.alertasStockBajo];
    }

    // Filtro por texto
    if (this.filtroTexto.trim()) {
      const texto = this.filtroTexto.toLowerCase().trim();
      base = base.filter(a => a.productoNombre.toLowerCase().includes(texto));
    }

    // Filtro por estado (días restantes)
    if (this.filtroEstado) {
      base = base.filter(a => {
        if (a.tipo !== 'proximo_vencer' || a.diasRestantes === undefined) {
          return false;
        }
        if (this.filtroEstado === 'critico') return a.diasRestantes <= 7;
        if (this.filtroEstado === 'advertencia') return a.diasRestantes > 7 && a.diasRestantes <= 30;
        if (this.filtroEstado === 'normal') return a.diasRestantes > 30;
        return true;
      });
    }

    // 🔥 FILTRO POR CATEGORÍA (AHORA FUNCIONAL)
    if (this.filtroCategoria) {
      base = base.filter(a => a.categoriaNombre === this.filtroCategoria);
    }

    this.alertasVisibles = base;
    this.paginaActual = 1;
    this.cdr.detectChanges();
  }

  // ======== GETTERS PARA PAGINACIÓN ========
  get alertasPaginadas(): Alerta[] {
    const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
    const fin = inicio + this.registrosPorPagina;
    return this.alertasVisibles.slice(inicio, fin);
  }

  get totalPaginas(): number {
    return Math.ceil(this.alertasVisibles.length / this.registrosPorPagina);
  }

  get maxMeses(): number {
    const max = Math.max(...this.mesesVencimiento.map(m => m.cantidad), 1);
    return max;
  }

  // ======== CAMBIAR PÁGINA ========
  cambiarPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas) {
      this.paginaActual = pagina;
    }
  }

  // ======== ESTADÍSTICAS ========
  contarVencidos(): number {
    return this.alertasProximosVencer.filter(
      a => a.diasRestantes !== undefined && a.diasRestantes < 0
    ).length;
  }

  contarCriticos(): number {
    return this.alertasProximosVencer.filter(
      a => a.diasRestantes !== undefined && a.diasRestantes >= 0 && a.diasRestantes <= 7
    ).length;
  }

  contarAdvertencia(): number {
    return this.alertasProximosVencer.filter(
      a => a.diasRestantes !== undefined && a.diasRestantes > 7 && a.diasRestantes <= 30
    ).length;
  }

  get mesesVencimiento(): { nombre: string; cantidad: number }[] {
    const meses = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    const conteo = new Array(12).fill(0);
    this.alertasProximosVencer.forEach(a => {
      if (a.fechaVencimiento) {
        const fecha = new Date(a.fechaVencimiento);
        conteo[fecha.getMonth()] += 1;
      }
    });
    return meses.map((nombre, i) => ({ nombre, cantidad: conteo[i] }));
  }

  // ======== MÉTODOS DE FILTRO ========
  cambiarFiltro(filtro: 'todas' | 'stock_bajo' | 'proximo_vencer'): void {
    this.filtroActivo = filtro;
    this.actualizarAlertasVisibles();
  }

  aplicarFiltros(): void {
    this.actualizarAlertasVisibles();
  }

  limpiarFiltros(): void {
    this.filtroTexto = '';
    this.filtroEstado = '';
    this.filtroCategoria = '';
    this.actualizarAlertasVisibles();
  }

  // ======== CLASES DE URGENCIA ========
  claseUrgencia(alerta: Alerta): string {
    if (alerta.tipo !== 'proximo_vencer' || alerta.diasRestantes === undefined) {
      return 'bg-warning text-dark';
    }
    if (alerta.diasRestantes <= 7) return 'bg-danger text-white';
    if (alerta.diasRestantes <= 15) return 'bg-warning text-dark';
    return 'bg-info text-white';
  }

  iconoAlerta(alerta: Alerta): string {
    return alerta.tipo === 'proximo_vencer' ? 'bi-hourglass-split' : 'bi-box-seam';
  }
}