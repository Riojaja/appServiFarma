import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { FabricanteService } from '../../../core/services/fabricante';
import { Fabricante } from '../../../core/models/fabricante.model';
import { AuthService } from '../../../core/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-listar-fabricantes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarFabricantesComponent implements OnInit {
  // Paginación
  itemsPorPagina: number = 7;
  paginaActual: number = 1;
  public Math = Math;

  // Datos
  fabricantes: Fabricante[] = [];
  fabricantesFiltrados: Fabricante[] = [];
  filtroNombre: string = '';
  cargando: boolean = false;
  private loaded: boolean = false;

  // Modal
  showModal: boolean = false;
  fabricanteEdit: Fabricante = { nombre: '', contacto: '', telefono: '', email: '' };
  esEdicion: boolean = false;

  // ======== PROTECCIÓN CONTRA DOBLE-CLICK ========
  guardando: boolean = false;      // bloquea el botón Guardar/Actualizar mientras hay una petición en curso
  eliminandoId: number | null = null; // id del fabricante que se está eliminando (bloquea su propio botón)

  // Permisos
  isAdmin: boolean = false;

  // Getters
  get totalFiltrados(): number {
    return this.fabricantesFiltrados.length;
  }

  get totalPaginas(): number {
    return Math.ceil(this.fabricantesFiltrados.length / this.itemsPorPagina);
  }

  get fabricantesPagina(): Fabricante[] {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    const fin = Math.min(inicio + this.itemsPorPagina, this.fabricantesFiltrados.length);
    return this.fabricantesFiltrados.slice(inicio, fin);
  }

  constructor(
    private fabricanteService: FabricanteService,
    public auth: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.isAdmin = this.auth.isAdmin();
    if (!this.loaded) {
      this.loaded = true;
      this.cargarFabricantes();
    }
  }

  cargarFabricantes(): void {
    this.cargando = true;
    this.fabricanteService.listar().subscribe({
      next: (data) => {
        this.fabricantes = data;
        this.aplicarFiltro();
        this.paginaActual = 1;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar fabricantes:', err);
        this.cargando = false;
        this.mostrarError('No se pudieron cargar los fabricantes.');
        this.cdr.detectChanges();
      }
    });
  }

  // ========== BÚSQUEDA INSTANTÁNEA (EN MEMORIA) ==========
  filtrarInstantaneo(): void {
    const texto = this.filtroNombre.trim().toLowerCase();

    if (!texto) {
      this.fabricantesFiltrados = [...this.fabricantes];
    } else {
      this.fabricantesFiltrados = this.fabricantes.filter(f =>
        f.nombre.toLowerCase().includes(texto) ||
        (f.contacto && f.contacto.toLowerCase().includes(texto)) ||
        (f.telefono && f.telefono.includes(texto)) ||
        (f.email && f.email.toLowerCase().includes(texto))
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
    this.fabricantesFiltrados = this.filtroNombre.trim()
      ? this.fabricantes.filter(f =>
          f.nombre.toLowerCase().includes(this.filtroNombre.trim().toLowerCase()) ||
          (f.contacto && f.contacto.toLowerCase().includes(this.filtroNombre.trim().toLowerCase())) ||
          (f.telefono && f.telefono.includes(this.filtroNombre.trim())) ||
          (f.email && f.email.toLowerCase().includes(this.filtroNombre.trim().toLowerCase()))
        )
      : [...this.fabricantes];
    this.cdr.detectChanges();
  }

  // Modal
  abrirModalCrear(): void {
    if (this.showModal) return; // evita doble apertura por doble-click
    this.fabricanteEdit = { nombre: '', contacto: '', telefono: '', email: '' };
    this.esEdicion = false;
    this.showModal = true;
    this.cdr.detectChanges();
  }

  abrirModalEditar(fabricante: Fabricante): void {
    if (this.showModal) return;
    this.fabricanteEdit = { ...fabricante };
    this.esEdicion = true;
    this.showModal = true;
    this.cdr.detectChanges();
  }

  cerrarModal(): void {
    if (this.guardando) return; // no se puede cerrar mientras se está guardando
    this.showModal = false;
    this.cdr.detectChanges();
  }

  guardarFabricante(form: NgForm): void {
    if (this.guardando) return; // 🔒 evita doble envío por doble-click

    if (form.invalid) {
      this.mostrarError('Debe completar todos los campos requeridos correctamente.');
      return;
    }

    this.guardando = true;

    const data = {
      nombre: this.fabricanteEdit.nombre,
      contacto: this.fabricanteEdit.contacto || '',
      telefono: this.fabricanteEdit.telefono || '',
      email: this.fabricanteEdit.email || ''
    };

    const observable = this.esEdicion
      ? this.fabricanteService.actualizar(this.fabricanteEdit.id!, data)
      : this.fabricanteService.crear(data);

    observable.subscribe({
      next: () => {
        this.guardando = false;
        this.cerrarModal();
        this.mostrarExito(
          this.esEdicion ? 'Fabricante actualizado' : 'Fabricante creado',
          this.esEdicion
            ? 'El fabricante se ha actualizado correctamente.'
            : 'El fabricante se ha registrado correctamente.'
        );
        this.cargarFabricantes();
      },
      error: (err) => {
        console.error('Error al guardar:', err);
        this.guardando = false;
        this.mostrarError(err.message || 'Error al guardar el fabricante.');
        this.cdr.detectChanges();
      }
    });
  }

  eliminar(id: number): void {
    if (!id || this.eliminandoId !== null) return; // 🔒 evita doble-click mientras hay una eliminación en curso
    const fabricante = this.fabricantes.find(f => f.id === id);
    if (!fabricante) return;

    Swal.fire({
      title: '¿Eliminar fabricante?',
      html: `
        <p style="color: #475569; margin-bottom: 8px;">
          ¿Está seguro de que desea <strong style="color: #dc2626;">ELIMINAR</strong> este fabricante?
        </p>
        <div style="background: #f8fafc; border-radius: 8px; padding: 12px; border-left: 4px solid #dc2626; text-align: left;">
          <strong>${fabricante.nombre}</strong><br>
          <span style="font-size: 0.85rem; color: #475569;">
            ${fabricante.contacto || 'Sin contacto'} | ${fabricante.telefono || 'Sin teléfono'}
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
        this.eliminandoId = id;
        this.cdr.detectChanges();

        this.fabricanteService.eliminar(id).subscribe({
          next: () => {
            this.eliminandoId = null;
            this.cargarFabricantes();
            this.mostrarExito('Eliminado', 'El fabricante ha sido eliminado correctamente.');
          },
          error: (err) => {
            console.error('Error al eliminar:', err);
            this.eliminandoId = null;
            this.mostrarError(err.message || 'No se pudo eliminar el fabricante.');
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  // Paginación
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

  // SweetAlert2
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