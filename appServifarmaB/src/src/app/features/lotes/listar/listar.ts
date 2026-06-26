import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LoteService } from '../../../core/services/lote';
import { Lote } from '../../../core/models/lote.model';

@Component({
  selector: 'app-listar-lotes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  lotes: Lote[] = [];
  filtroProducto: string = '';
  filtroVencimiento: string = ''; // 'proximo' o vacío
  cargando: boolean = false;

  constructor(
    private loteService: LoteService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Leer la URL para detectar si estamos en 'proximos-a-vencer'
    this.route.url.subscribe(url => {
      const path = url.map(seg => seg.path).join('/');
      if (path.includes('proximos-a-vencer')) {
        this.filtroVencimiento = 'proximo';
      } else {
        this.filtroVencimiento = '';
      }
      this.cargarLotes();
    });
  }

  cargarLotes(): void {
    this.cargando = true;
    this.loteService.listar().subscribe({
      next: (data: Lote[]) => {
        let lotesFiltrados = data;

        // Si el filtro de vencimiento está activo, filtrar lotes que vencen en ≤30 días
        if (this.filtroVencimiento === 'proximo') {
          const hoy = new Date();
          hoy.setHours(0, 0, 0, 0);
          const limite = new Date(hoy);
          limite.setDate(limite.getDate() + 30);

          lotesFiltrados = data.filter(lote => {
            if (lote.estado !== 'activo') return false;
            const fechaVenc = new Date(lote.fechaVencimiento);
            fechaVenc.setHours(0, 0, 0, 0);
            return fechaVenc >= hoy && fechaVenc <= limite;
          });
        }

        this.lotes = lotesFiltrados;
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar lotes:', err);
        this.cargando = false;
      }
    });
  }

  buscar(): void {
    if (this.filtroProducto.trim()) {
      const id = Number(this.filtroProducto);
      if (!isNaN(id)) {
        this.loteService.listarPorProducto(id).subscribe({
          next: (data: Lote[]) => this.lotes = data,
          error: (err: any) => console.error('Error al buscar:', err)
        });
      } else {
        this.loteService.buscarPorLote(this.filtroProducto).subscribe({
          next: (data: Lote) => this.lotes = [data],
          error: (err: any) => {
            if (err.status === 404) {
              this.lotes = [];
            } else {
              console.error('Error al buscar:', err);
            }
          }
        });
      }
    } else {
      this.cargarLotes();
    }
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este lote?')) {
      this.loteService.eliminar(id).subscribe({
        next: () => {
          this.lotes = this.lotes.filter(l => l.id !== id);
        },
        error: (err: any) => console.error('Error al eliminar:', err)
      });
    }
  }

  marcarDeteriorado(id: number): void {
    if (confirm('¿Marcar este lote como deteriorado? Esta acción retira el lote de la venta activa.')) {
      this.loteService.marcarDeteriorado(id).subscribe({
        next: () => {
          const lote = this.lotes.find(l => l.id === id);
          if (lote) {
            lote.estado = 'deteriorado';
          }
        },
        error: (err: any) => {
          console.error('Error al marcar como deteriorado:', err);
          alert(err.error?.mensaje || 'No se pudo marcar el lote como deteriorado');
        }
      });
    }
  }

  getEstadoBadge(estado: string): string {
    const map: { [key: string]: string } = {
      'activo': 'bg-success',
      'deteriorado': 'bg-warning',
      'vencido': 'bg-danger',
      'agotado': 'bg-secondary'
    };
    return map[estado] || 'bg-secondary';
  }
}