import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuditoriaService } from '../../../core/services/auditoria';
import { MovimientoStock } from '../../../core/models/movimiento-stock.model';

@Component({
  selector: 'app-auditoria-movimientos-stock',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './movimientos-stock.html',
  styleUrls: ['./movimientos-stock.css']
})
export class MovimientosStockComponent implements OnInit {
  movimientos: MovimientoStock[] = [];
  filtros = {
    loteId: '',
    tipo: '',
    fechaInicio: '',
    fechaFin: ''
  };
  cargando: boolean = false;
  tipos = ['compra', 'venta', 'ajuste', 'merma'];

  constructor(private auditoriaService: AuditoriaService) { }

  ngOnInit(): void {
    this.cargarMovimientos();
  }

  cargarMovimientos(): void {
    this.cargando = true;
    // Usar un rango de fechas por defecto (último mes)
    if (!this.filtros.fechaInicio || !this.filtros.fechaFin) {
      const hoy = new Date();
      const mesAtras = new Date(hoy);
      mesAtras.setMonth(mesAtras.getMonth() - 1);
      this.filtros.fechaInicio = mesAtras.toISOString().split('T')[0];
      this.filtros.fechaFin = hoy.toISOString().split('T')[0];
    }

    // Si hay filtro de lote
    if (this.filtros.loteId) {
      this.auditoriaService.obtenerHistorialLote(Number(this.filtros.loteId)).subscribe({
        next: (data) => {
          this.movimientos = data;
          this.cargando = false;
        },
        error: (err) => {
          console.error('Error:', err);
          this.cargando = false;
        }
      });
      return;
    }

    // Si hay filtro de tipo
    if (this.filtros.tipo) {
      this.auditoriaService.obtenerMovimientosPorTipo(
        this.filtros.tipo,
        this.filtros.fechaInicio,
        this.filtros.fechaFin
      ).subscribe({
        next: (data) => {
          this.movimientos = data;
          this.cargando = false;
        },
        error: (err) => {
          console.error('Error:', err);
          this.cargando = false;
        }
      });
      return;
    }

    // Si no hay filtros específicos, cargar todos (con fechas)
    // Por simplicidad, cargamos todos los movimientos (sin filtro)
    // Nota: podríamos usar el servicio general, pero no tiene listar sin filtros.
    // Mejor usamos el de tipo vacío.
    this.auditoriaService.obtenerMovimientosPorTipo('', this.filtros.fechaInicio, this.filtros.fechaFin).subscribe({
      next: (data) => {
        this.movimientos = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error:', err);
        this.cargando = false;
      }
    });
  }

  limpiarFiltros(): void {
    this.filtros = { loteId: '', tipo: '', fechaInicio: '', fechaFin: '' };
    this.cargarMovimientos();
  }
}