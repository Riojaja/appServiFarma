import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { InventarioService } from '../../../core/services/inventario';
import { Alerta } from '../../../core/models/alerta.model';

interface ResumenInventario {
  totalProductosStockBajo: number;
  totalProductosSinStock: number;
  totalLotesProximosAVencer: number;
  totalLotesActivosConStock: number;
}

@Component({
  selector: 'app-alertas',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './alertas.html',
  styleUrl: './alertas.css',
})
export class Alertas implements OnInit {
  alertasStockBajo: Alerta[] = [];
  alertasProximosVencer: Alerta[] = [];
  resumen: ResumenInventario | null = null;

  filtroActivo: 'todas' | 'stock_bajo' | 'proximo_vencer' = 'todas';
  cargando = true;
  error = false;

  constructor(private inventarioService: InventarioService) { }

  ngOnInit(): void {
    this.cargarAlertas();
  }

  cargarAlertas(): void {
    this.cargando = true;
    this.error = false;

    forkJoin({
      stockBajo: this.inventarioService.obtenerStockBajo(),
      proximosVencer: this.inventarioService.obtenerProximosVencer(),
      resumen: this.inventarioService.obtenerResumen()
    }).subscribe({
      next: ({ stockBajo, proximosVencer, resumen }) => {
        this.alertasStockBajo = stockBajo;
        this.alertasProximosVencer = proximosVencer;
        this.resumen = resumen;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar alertas de inventario:', err);
        this.error = true;
        this.cargando = false;
      }
    });
  }

  get alertasVisibles(): Alerta[] {
    if (this.filtroActivo === 'stock_bajo') {
      return this.alertasStockBajo;
    }
    if (this.filtroActivo === 'proximo_vencer') {
      return this.alertasProximosVencer;
    }
    // 'todas': próximos a vencer primero (más urgente por fecha), luego stock bajo
    return [...this.alertasProximosVencer, ...this.alertasStockBajo];
  }

  cambiarFiltro(filtro: 'todas' | 'stock_bajo' | 'proximo_vencer'): void {
    this.filtroActivo = filtro;
  }

  /** Clase visual según qué tan urgente es un lote próximo a vencer. */
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