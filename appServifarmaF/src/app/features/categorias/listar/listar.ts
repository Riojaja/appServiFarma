import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { CategoriaService } from '../../../core/services/categoria';
import { Categoria } from '../../../core/models/categoria.model';
import { AuthService } from '../../../core/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-listar-categorias',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit, OnDestroy {
  // ========== PAGINACIÓN ==========
  itemsPorPagina: number = 7;
  paginaActual: number = 1;
  public Math = Math; // Para usar en la plantilla

  // ========== DATOS ==========
  categorias: Categoria[] = [];
  categoriasFiltradas: Categoria[] = [];
  filtroNombre: string = '';
  cargando: boolean = false;
  private loaded: boolean = false; // Evita doble carga

  // ========== MODAL ==========
  showModal: boolean = false;
  categoriaEdit: Categoria = { nombre: '', descripcion: '' };
  esEdicion: boolean = false;

  // ========== PERMISOS ==========
  isAdmin: boolean = false;

  // ========== GETTERS ==========
  get totalFiltrados(): number {
    return this.categoriasFiltradas.length;
  }

  get totalPaginas(): number {
    return Math.ceil(this.categoriasFiltradas.length / this.itemsPorPagina);
  }

  get categoriasPagina(): Categoria[] {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    const fin = Math.min(inicio + this.itemsPorPagina, this.categoriasFiltradas.length);
    return this.categoriasFiltradas.slice(inicio, fin);
  }

  constructor(
    private categoriaService: CategoriaService,
    public auth: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.isAdmin = this.auth.isAdmin();
    if (!this.loaded) {
      this.loaded = true;
      this.cargarCategorias();
      // No necesitamos polling aquí (no es una bitácora)
    }
  }

  ngOnDestroy(): void {
    // No hay interval, pero por si acaso
  }

  // ========== CARGAR CATEGORÍAS ==========
  cargarCategorias(): void {
    this.cargando = true;
    this.categoriaService.listar().subscribe({
      next: (data) => {
        this.categorias = data;
        this.aplicarFiltro();
        this.paginaActual = 1;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar categorías:', err);
        this.cargando = false;
        this.mostrarError('No se pudieron cargar las categorías.');
        this.cdr.detectChanges();
      }
    });
  }

  // ========== BÚSQUEDA INSTANTÁNEA (EN MEMORIA) ==========
  // Se ejecuta en cada tecla gracias a (ngModelChange) en el template.
  // Ya no depende del backend ni de presionar Enter: filtra al instante
  // sobre los datos que ya están cargados en `this.categorias`.
  filtrarInstantaneo(): void {
    const texto = this.filtroNombre.trim().toLowerCase();

    if (!texto) {
      this.categoriasFiltradas = [...this.categorias];
    } else {
      this.categoriasFiltradas = this.categorias.filter(c =>
        c.nombre.toLowerCase().includes(texto) ||
        (c.descripcion && c.descripcion.toLowerCase().includes(texto))
      );
    }

    this.paginaActual = 1;
    this.cdr.detectChanges();
  }

  limpiarFiltro(): void {
    this.filtroNombre = '';
    this.filtrarInstantaneo();
  }

  private aplicarFiltro(): void {
    // Punto de partida al cargar/recargar: sin filtro, se muestran todas.
    this.categoriasFiltradas = this.filtroNombre.trim()
      ? this.categorias.filter(c =>
          c.nombre.toLowerCase().includes(this.filtroNombre.trim().toLowerCase()) ||
          (c.descripcion && c.descripcion.toLowerCase().includes(this.filtroNombre.trim().toLowerCase()))
        )
      : [...this.categorias];
    this.cdr.detectChanges();
  }

  // ========== MODAL ==========
  abrirModalCrear(): void {
    this.categoriaEdit = { nombre: '', descripcion: '' };
    this.esEdicion = false;
    this.showModal = true;
    this.cdr.detectChanges();
  }

  abrirModalEditar(categoria: Categoria): void {
    this.categoriaEdit = { ...categoria }; // Copia para no modificar original
    this.esEdicion = true;
    this.showModal = true;
    this.cdr.detectChanges();
  }

  cerrarModal(): void {
    this.showModal = false;
    this.cdr.detectChanges();
  }

  guardarCategoria(form: NgForm): void {
    if (form.invalid) {
      this.mostrarError('Debe completar todos los campos requeridos.');
      return;
    }

    const data = {
      nombre: this.categoriaEdit.nombre,
      descripcion: this.categoriaEdit.descripcion || ''
    };

    const observable = this.esEdicion
      ? this.categoriaService.actualizar(this.categoriaEdit.id!, data)
      : this.categoriaService.crear(data);

    observable.subscribe({
      next: () => {
        this.cerrarModal();
        this.mostrarExito(
          this.esEdicion ? 'Categoría actualizada' : 'Categoría creada',
          this.esEdicion
            ? 'La categoría se ha actualizado correctamente.'
            : 'La categoría se ha registrado correctamente.'
        );
        this.cargarCategorias();
      },
      error: (err) => {
        console.error('Error al guardar:', err);
        this.mostrarError(err.message || 'Error al guardar la categoría.');
      }
    });
  }

  // ========== ELIMINAR ==========
  eliminar(id: number): void {
    if (!id) return;
    const categoria = this.categorias.find(c => c.id === id);
    if (!categoria) return;

    Swal.fire({
      title: '¿Eliminar categoría?',
      html: `
        <p style="color: #475569; margin-bottom: 8px;">
          ¿Está seguro de que desea <strong style="color: #dc2626;">ELIMINAR</strong> esta categoría?
        </p>
        <div style="background: #f8fafc; border-radius: 8px; padding: 12px; border-left: 4px solid #dc2626; text-align: left;">
          <strong>${categoria.nombre}</strong><br>
          <span style="font-size: 0.85rem; color: #475569;">
            ${categoria.descripcion || 'Sin descripción'}
          </span>
        </div>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (result.isConfirmed) {
        this.categoriaService.eliminar(id).subscribe({
          next: () => {
            this.cargarCategorias();
            this.mostrarExito('Eliminado', 'La categoría ha sido eliminada correctamente.');
          },
          error: (err) => {
            console.error('Error al eliminar:', err);
            this.mostrarError(err.message || 'No se pudo eliminar la categoría.');
          }
        });
      }
    });
  }

  // ========== PAGINACIÓN ==========
  siguientePagina(): void {
    if (this.paginaActual < this.totalPaginas) {
      this.paginaActual++;
      this.cdr.detectChanges();
    }
  }

  anteriorPagina(): void {
    if (this.paginaActual > 1) {
      this.paginaActual--;
      this.cdr.detectChanges();
    }
  }

  // ========== SWEETALERT2 ==========
  private mostrarExito(titulo: string, mensaje: string): void {
    Swal.fire({
      icon: 'success',
      title: titulo,
      text: mensaje,
      timer: 3000,
      showConfirmButton: false,
      customClass: { popup: 'swal-farmaceutico' }
    });
  }

  private mostrarError(mensaje: string): void {
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: mensaje,
      confirmButtonColor: '#dc2626',
      confirmButtonText: 'Aceptar',
      customClass: { popup: 'swal-farmaceutico' }
    });
  }
}