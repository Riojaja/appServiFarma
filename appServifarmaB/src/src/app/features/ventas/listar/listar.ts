import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VentaService } from '../../../core/services/venta';
import { Venta } from '../../../core/models/venta.model';

@Component({
  selector: 'app-listar-ventas',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  ventas: Venta[] = [];
  filtros = {
    fechaInicio: '',
    fechaFin: '',
    cliente: '',
    usuario: '',
    estado: ''
  };
  cargando: boolean = false;

  constructor(private ventaService: VentaService) { }

  ngOnInit(): void {
    this.cargarVentas();
  }

  cargarVentas(): void {
    this.cargando = true;
    this.ventaService.listar().subscribe({
      next: (data) => {
        this.ventas = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error:', err);
        this.cargando = false;
      }
    });
  }

  aplicarFiltros(): void {
    // Implementación de filtros según los criterios del backend
    // Puedes hacer llamadas específicas con los servicios.
    // Por simplicidad, recargamos todos y filtramos localmente (para demostración)
    this.cargarVentas();
  }

  limpiarFiltros(): void {
    this.filtros = { fechaInicio: '', fechaFin: '', cliente: '', usuario: '', estado: '' };
    this.cargarVentas();
  }

  getEstadoBadge(estado: string): string {
    return estado === 'completada' ? 'bg-success' : 'bg-danger';
  }
}