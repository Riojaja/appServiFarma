import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
// Importamos desde las carpetas centralizadas
import { CategoriaService } from '../../../core/services/categoria';
import { Categoria } from '../../../core/models/categoria.model';

@Component({
  selector: 'app-listar-categorias',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  categorias: Categoria[] = [];
  filtroNombre: string = '';
  cargando: boolean = false;

  constructor(private categoriaService: CategoriaService) { }

  ngOnInit(): void {
    this.cargarCategorias();
  }

  cargarCategorias(): void {
    this.cargando = true;
    this.categoriaService.listar().subscribe({
      next: (data: Categoria[]) => {
        this.categorias = data;
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar categorías:', err);
        this.cargando = false;
      }
    });
  }

  buscar(): void {
    if (this.filtroNombre.trim()) {
      this.categoriaService.buscarPorNombre(this.filtroNombre).subscribe({
        next: (data: Categoria[]) => this.categorias = data,
        error: (err: any) => console.error('Error al buscar:', err)
      });
    } else {
      this.cargarCategorias();
    }
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar esta categoría?')) {
      this.categoriaService.eliminar(id).subscribe({
        next: () => {
          this.categorias = this.categorias.filter(c => c.id !== id);
        },
        error: (err: any) => console.error('Error al eliminar:', err)
      });
    }
  }
}