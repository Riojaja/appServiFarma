import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { ProveedorService } from '../../../core/services/proveedor';
import { Proveedor } from '../../../core/models/proveedor.model';
import { AuthService } from '../../../core/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-listar-proveedores',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarProveedoresComponent implements OnInit {
  public Math = Math;

  // Datos
  proveedores: Proveedor[] = [];
  proveedoresFiltrados: Proveedor[] = [];
  filtroBusqueda: string = ''; // Busca por razón social o RUC
  cargando: boolean = false;
  private loaded: boolean = false;

  // Modal
  showModal: boolean = false;
  proveedorEdit: Proveedor = {
    ruc: '',
    razonSocial: '',
    direccion: '',
    telefono: '',
    email: '',
    contacto: '',
    region: ''
  };
  esEdicion: boolean = false;

  // ======== PROTECCIÓN CONTRA DOBLE-CLICK ========
  guardando: boolean = false;
  eliminandoId: number | null = null;

  // Filtro región
  regiones: string[] = [
    'Amazonas', 'Áncash', 'Apurímac', 'Arequipa', 'Ayacucho',
    'Cajamarca', 'Callao', 'Cusco', 'Huancavelica', 'Huánuco',
    'Ica', 'Junín', 'La Libertad', 'Lambayeque', 'Lima',
    'Loreto', 'Madre de Dios', 'Moquegua', 'Pasco', 'Piura',
    'Puno', 'San Martín', 'Tacna', 'Tumbes', 'Ucayali'
  ];
  filtroRegion: string = '';

  // Permisos
  isAdmin: boolean = false;

  // Getters
  get totalFiltrados(): number {
    return this.proveedoresFiltrados.length;
  }

  constructor(
    private proveedorService: ProveedorService,
    public auth: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.isAdmin = this.auth.isAdmin();
    if (!this.loaded) {
      this.loaded = true;
      this.cargarProveedores();
      this.cargarRegiones();
    }
  }

  cargarProveedores(): void {
    this.cargando = true;
    this.proveedorService.listar().subscribe({
      next: (data) => {
        this.proveedores = data;
        this.aplicarFiltros();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar proveedores:', err);
        this.cargando = false;
        this.mostrarError('No se pudieron cargar los proveedores.');
        this.cdr.detectChanges();
      }
    });
  }

  cargarRegiones(): void {
    this.proveedorService.obtenerRegiones().subscribe({
      next: (data) => this.regiones = data,
      error: (err) => console.error('Error al cargar regiones:', err)
    });
  }

  // ========== BÚSQUEDA + FILTRO DE REGIÓN, INSTANTÁNEOS (EN MEMORIA) ==========
  filtrarInstantaneo(): void {
    this.aplicarFiltros();
  }

  limpiarFiltro(): void {
    this.filtroBusqueda = '';
    this.filtroRegion = '';
    this.aplicarFiltros();
  }

  private aplicarFiltros(): void {
    let filtrados = [...this.proveedores];

    if (this.filtroRegion) {
      filtrados = filtrados.filter(p => p.region === this.filtroRegion);
    }

    const texto = this.filtroBusqueda.trim().toLowerCase();
    if (texto) {
      filtrados = filtrados.filter(p =>
        p.razonSocial.toLowerCase().includes(texto) ||
        p.ruc.includes(texto) ||
        (p.contacto && p.contacto.toLowerCase().includes(texto)) ||
        (p.telefono && p.telefono.includes(texto)) ||
        (p.email && p.email.toLowerCase().includes(texto))
      );
    }

    this.proveedoresFiltrados = filtrados;
    this.cdr.detectChanges();
  }

  // ======== COLOR DE AVATAR SEGÚN EL NOMBRE ========
  obtenerColorAvatar(nombre: string): string {
    const colores = [
      '#0d9488', // Verde menta
      '#3b82f6', // Azul
      '#8b5cf6', // Morado
      '#f59e0b', // Amarillo
      '#ec4899', // Rosa
      '#14b8a6', // Verde petróleo
      '#f97316', // Naranja
      '#6366f1'  // Índigo
    ];
    const index = Math.abs(nombre.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)) % colores.length;
    return colores[index];
  }

  // Modal
  abrirModalCrear(): void {
    if (this.showModal) return;
    this.proveedorEdit = {
      ruc: '',
      razonSocial: '',
      direccion: '',
      telefono: '',
      email: '',
      contacto: '',
      region: ''
    };
    this.esEdicion = false;
    this.showModal = true;
    this.cdr.detectChanges();
  }

  abrirModalEditar(proveedor: Proveedor): void {
    if (this.showModal) return;
    this.proveedorEdit = { ...proveedor };
    this.esEdicion = true;
    this.showModal = true;
    this.cdr.detectChanges();
  }

  cerrarModal(): void {
    if (this.guardando) return;
    this.showModal = false;
    this.cdr.detectChanges();
  }

  guardarProveedor(form: NgForm): void {
    if (this.guardando) return; // 🔒 evita doble envío por doble-click

    if (form.invalid) {
      this.mostrarError('Debe completar todos los campos requeridos correctamente.');
      return;
    }

    this.guardando = true;

    const data = {
      ruc: this.proveedorEdit.ruc,
      razonSocial: this.proveedorEdit.razonSocial,
      direccion: this.proveedorEdit.direccion || '',
      telefono: this.proveedorEdit.telefono || '',
      email: this.proveedorEdit.email || '',
      contacto: this.proveedorEdit.contacto || '',
      region: this.proveedorEdit.region || ''
    };

    const observable = this.esEdicion
      ? this.proveedorService.actualizar(this.proveedorEdit.id!, data)
      : this.proveedorService.crear(data);

    observable.subscribe({
      next: () => {
        this.guardando = false;
        this.cerrarModal();
        this.mostrarExito(
          this.esEdicion ? 'Proveedor actualizado' : 'Proveedor creado',
          this.esEdicion
            ? 'El proveedor se ha actualizado correctamente.'
            : 'El proveedor se ha registrado correctamente.'
        );
        this.cargarProveedores();
      },
      error: (err) => {
        console.error('Error al guardar:', err);
        this.guardando = false;
        this.mostrarError(err.message || 'Error al guardar el proveedor.');
        this.cdr.detectChanges();
      }
    });
  }

  eliminar(id: number): void {
    if (!id || this.eliminandoId !== null) return; // 🔒 evita doble-click
    const proveedor = this.proveedores.find(p => p.id === id);
    if (!proveedor) return;

    Swal.fire({
      title: '¿Eliminar proveedor?',
      html: `
        <p style="color: #475569; margin-bottom: 8px;">
          ¿Está seguro de que desea <strong style="color: #dc2626;">ELIMINAR</strong> este proveedor?
        </p>
        <div style="background: #f8fafc; border-radius: 8px; padding: 12px; border-left: 4px solid #dc2626; text-align: left;">
          <strong>${proveedor.razonSocial}</strong><br>
          <span style="font-size: 0.85rem; color: #475569;">
            RUC: ${proveedor.ruc} | ${proveedor.contacto || 'Sin contacto'}
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

        this.proveedorService.eliminar(id).subscribe({
          next: () => {
            this.eliminandoId = null;
            this.cargarProveedores();
            this.mostrarExito('Eliminado', 'El proveedor ha sido eliminado correctamente.');
          },
          error: (err) => {
            console.error('Error al eliminar:', err);
            this.eliminandoId = null;
            const mensaje = this.extraerMensajeError(
              err,
              'No se pudo eliminar el proveedor. Es posible que tenga productos, órdenes de compra u otros registros asociados.'
            );
            this.mostrarError(mensaje);
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  // ======== EXTRAER LA CAUSA REAL DEL ERROR (no el mensaje genérico de HttpClient) ========
  // Los backends suelen mandar el detalle en distintos formatos según el error
  // (validación, constraint de base de datos, etc.). Probamos varias rutas comunes
  // antes de caer al mensaje genérico.
  private extraerMensajeError(err: any, fallback: string): string {
    if (!err) return fallback;

    // Body de error como string plano
    if (typeof err.error === 'string' && err.error.trim()) {
      return err.error;
    }

    // Formatos típicos: { mensaje }, { message }, { error }, { detail }
    const posibles = [
      err.error?.mensaje,
      err.error?.message,
      err.error?.error,
      err.error?.detail,
      err.error?.errors?.[0]?.mensaje,
      err.error?.errors?.[0]?.message
    ];
    const encontrado = posibles.find(m => typeof m === 'string' && m.trim().length > 0);
    if (encontrado) return encontrado;

    // Conflictos típicos de FK (proveedor con productos/órdenes asociadas)
    if (err.status === 409 || err.status === 500) {
      return fallback;
    }

    return fallback;
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