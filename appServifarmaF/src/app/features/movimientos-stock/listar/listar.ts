import { Component, OnInit } from '@angular/core';
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
  movimientos: MovimientoStock[] = [];
  filtroLote: string = '';
  filtroTipo: string = '';
  filtroFechaInicio: string = '';
  filtroFechaFin: string = '';
  cargando: boolean = false;

  tiposMovimiento = ['compra', 'venta', 'ajuste', 'merma'];

  constructor(private movimientoService: MovimientoStockService) { }

  ngOnInit(): void {
    this.cargarMovimientos();
  }

  cargarMovimientos(): void {
    this.cargando = true;
    this.movimientoService.listar().subscribe({
      next: (data: MovimientoStock[]) => {
        this.movimientos = data;
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar movimientos:', err);
        this.cargando = false;
      }
    });
  }

  aplicarFiltros(): void {
    // Si hay filtro de lote (asumimos ID)
    if (this.filtroLote.trim()) {
      const loteId = Number(this.filtroLote);
      if (!isNaN(loteId)) {
        if (this.filtroTipo) {
          this.movimientoService.listarPorLoteYTipo(loteId, this.filtroTipo).subscribe({
            next: (data: MovimientoStock[]) => this.movimientos = data,
            error: (err: any) => console.error('Error:', err)
          });
        } else {
          this.movimientoService.listarPorLote(loteId).subscribe({
            next: (data: MovimientoStock[]) => this.movimientos = data,
            error: (err: any) => console.error('Error:', err)
          });
        }
        return;
      }
    }

    // Filtro por tipo
    if (this.filtroTipo) {
      this.movimientoService.listarPorTipo(this.filtroTipo).subscribe({
        next: (data: MovimientoStock[]) => this.movimientos = data,
        error: (err: any) => console.error('Error:', err)
      });
      return;
    }

    // Filtro por fechas
    if (this.filtroFechaInicio && this.filtroFechaFin) {
      this.movimientoService.listarPorFecha(this.filtroFechaInicio, this.filtroFechaFin).subscribe({
        next: (data: MovimientoStock[]) => this.movimientos = data,
        error: (err: any) => console.error('Error:', err)
      });
      return;
    }

    // Si no hay filtros, recargar todos
    this.cargarMovimientos();
  }

  limpiarFiltros(): void {
    this.filtroLote = '';
    this.filtroTipo = '';
    this.filtroFechaInicio = '';
    this.filtroFechaFin = '';
    this.cargarMovimientos();
  }

  getTipoBadge(tipo: string): string {
    const map: { [key: string]: string } = {
      'compra': 'bg-success',
      'venta': 'bg-primary',
      'ajuste': 'bg-warning',
      'merma': 'bg-danger'
    };
    return map[tipo] || 'bg-secondary';
  }
}