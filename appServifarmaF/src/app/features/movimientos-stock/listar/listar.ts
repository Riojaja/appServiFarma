import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MovimientoStockService } from '../../../core/services/movimiento-stock';
import { MovimientoStock } from '../../../core/models/movimiento-stock.model';

@Component({
  selector: 'app-listar-movimientos-stock',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  // ======== DATOS ========
  movimientos: MovimientoStock[] = [];
  movimientosFiltrados: MovimientoStock[] = [];

  // ======== FILTROS ========
  filtroLote: string = '';
  filtroTipo: string = '';
  filtroFechaInicio: string = '';
  filtroFechaFin: string = '';

  tiposMovimiento = ['compra', 'venta', 'ajuste', 'merma'];

  // ======== PAGINACIÓN ========
  paginaActual: number = 1;
  registrosPorPagina: number = 14;
  Math = Math;

  // ======== ESTADOS ========
  cargando: boolean = false;
  error: string = '';

  constructor(
    private movimientoService: MovimientoStockService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.cargarMovimientos();
  }

  // ======== CARGA DE DATOS (CON CARGA INSTANTÁNEA Y ORDEN DESCENDENTE) ========
  cargarMovimientos(): void {
    this.cargando = true;
    this.error = '';

    this.movimientoService.listar().subscribe({
      next: (data: MovimientoStock[]) => {
        // 🔥 ORDENAR DESCENDENTE POR FECHA (el más reciente primero)
        this.movimientos = data.sort((a, b) =>
          new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
        );
        this.aplicarFiltros();
        this.cargando = false;
        // Forzar detección de cambios para que el spinner desaparezca inmediatamente
        setTimeout(() => this.cdr.detectChanges(), 0);
      },
      error: (err: any) => {
        console.error('Error al cargar movimientos:', err);
        this.error = 'Error al cargar los movimientos. Intente de nuevo.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ======== APLICAR FILTROS (AUTOMÁTICO) ========
  aplicarFiltros(): void {
    let base = [...this.movimientos];

    // Filtro por lote
    if (this.filtroLote.trim()) {
      const loteId = Number(this.filtroLote);
      if (!isNaN(loteId)) {
        base = base.filter(m => m.loteId === loteId);
      }
    }

    // Filtro por tipo
    if (this.filtroTipo) {
      base = base.filter(m => m.tipoMovimiento === this.filtroTipo);
    }

    // Filtro por fechas
    if (this.filtroFechaInicio) {
      const fechaInicio = new Date(this.filtroFechaInicio);
      fechaInicio.setHours(0, 0, 0, 0);
      base = base.filter(m => new Date(m.fecha) >= fechaInicio);
    }
    if (this.filtroFechaFin) {
      const fechaFin = new Date(this.filtroFechaFin);
      fechaFin.setHours(23, 59, 59, 999);
      base = base.filter(m => new Date(m.fecha) <= fechaFin);
    }

    this.movimientosFiltrados = base;
    this.paginaActual = 1; // Reiniciar paginación al aplicar filtros
  }

  // ======== LIMPIAR FILTROS ========
  limpiarFiltros(): void {
    this.filtroLote = '';
    this.filtroTipo = '';
    this.filtroFechaInicio = '';
    this.filtroFechaFin = '';
    this.aplicarFiltros();
  }

  // ======== PAGINACIÓN ========
  get movimientosPaginados(): MovimientoStock[] {
    const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
    const fin = inicio + this.registrosPorPagina;
    return this.movimientosFiltrados.slice(inicio, fin);
  }

  get totalPaginas(): number {
    return Math.ceil(this.movimientosFiltrados.length / this.registrosPorPagina);
  }

  cambiarPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas) {
      this.paginaActual = pagina;
    }
  }

  // ======== UTILIDAD - BADGES CON COLORES SUAVES ========
  getTipoBadge(tipo: string): string {
    const map: { [key: string]: string } = {
      'compra': 'badge-tipo-compra',
      'venta': 'badge-tipo-venta',
      'ajuste': 'badge-tipo-ajuste',
      'merma': 'badge-tipo-merma'
    };
    return map[tipo] || 'badge-tipo-otro';
  }
}