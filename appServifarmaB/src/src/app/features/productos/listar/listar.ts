import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../../core/services/producto';
import { CategoriaService } from '../../../core/services/categoria';
import { Producto } from '../../../core/models/producto.model';
import { Categoria } from '../../../core/models/categoria.model';

@Component({
  selector: 'app-listar-productos',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  productos: Producto[] = [];
  productosFiltrados: Producto[] = [];
  categorias: Categoria[] = [];
  filtroTexto: string = '';
  filtroCategoria: string = '';
  filtroEstado: string = '';
  cargando: boolean = false;

  constructor(
    private productoService: ProductoService,
    private categoriaService: CategoriaService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.cargarCategorias();
    this.route.queryParams.subscribe(params => {
      if (params['stock'] === 'bajo') {
        this.filtroEstado = 'sin-stock';
      }
      this.cargarProductos();
    });
  }

  cargarCategorias(): void {
    this.categoriaService.listar().subscribe({
      next: (data) => this.categorias = data,
      error: (err) => console.error('Error al cargar categorías:', err)
    });
  }

  getStock(producto: Producto): number {
    return producto.stockActual ?? 0;
  }

  cargarProductos(): void {
    this.cargando = true;
    this.productoService.listar().subscribe({
      next: (data: Producto[]) => {
        this.productos = data.map(p => ({
          ...p,
          stockActual: this.calcularStock(p.id!)
        }));
        this.aplicarFiltros();
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar productos:', err);
        this.cargando = false;
      }
    });
  }

  private calcularStock(productoId: number): number {
    // Simulación de stock (reemplazar con llamada real)
    // En producción: this.productoService.obtenerStock(productoId).subscribe(...)
    const stocks: { [key: number]: number } = {
      1: 12,
      2: 5,
      3: 18,
      4: 0,
      5: 8,
      6: 25
    };
    return stocks[productoId] ?? Math.floor(Math.random() * 30);
  }

  buscar(): void {
    if (this.filtroTexto.trim()) {
      this.productoService.buscarPorNombreOCodigo(this.filtroTexto).subscribe({
        next: (data: Producto[]) => {
          this.productos = data.map(p => ({
            ...p,
            stockActual: this.calcularStock(p.id!)
          }));
          this.aplicarFiltros();
        },
        error: (err: any) => console.error('Error al buscar:', err)
      });
    } else {
      this.cargarProductos();
    }
  }

  aplicarFiltros(): void {
    this.productosFiltrados = this.productos.filter(p => {
      if (this.filtroCategoria && p.categoriaId !== Number(this.filtroCategoria)) {
        return false;
      }
      if (this.filtroEstado === 'con-stock' && (p.stockActual || 0) <= 0) {
        return false;
      }
      if (this.filtroEstado === 'sin-stock' && (p.stockActual || 0) > 0) {
        return false;
      }
      return true;
    });
  }

  limpiarFiltros(): void {
    this.filtroTexto = '';
    this.filtroCategoria = '';
    this.filtroEstado = '';
    this.router.navigate(['/productos']);
    this.cargarProductos();
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este producto?')) {
      this.productoService.eliminar(id).subscribe({
        next: () => {
          this.productos = this.productos.filter(p => p.id !== id);
          this.aplicarFiltros();
        },
        error: (err: any) => console.error('Error al eliminar:', err)
      });
    }
  }
}