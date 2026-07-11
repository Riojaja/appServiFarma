import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { BitacoraService } from '../../../core/services/bitacora';
import { BitacoraComunicacion, BitacoraComunicacionRequest } from '../../../core/models/bitacora-comunicacion.model';
import { AuthService } from '../../../core/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-listar-bitacora',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit, OnDestroy {
  // ========== PAGINACIÓN ==========
  itemsPorPagina: number = 7;
  paginaActual: number = 1;

  mensajes: BitacoraComunicacion[] = [];
  mensajesFiltrados: BitacoraComunicacion[] = [];
  cargando: boolean = false;
  filtroTipo: string = 'todos';
  private pollingInterval: any;
  isAdmin: boolean = false;
  private loaded: boolean = false;

  // ========== MODAL ==========
  showModal: boolean = false;
  nuevoMensaje = { mensaje: '', tipo: 'novedad' };

  // ========== MATH PARA LA PLANTILLA ==========
  public Math = Math;

  // ========== GETTERS ==========
  get totalMensajes(): number {
    return this.mensajes.length;
  }

  get totalNoLeidos(): number {
    return this.mensajes.filter(m => !m.leido).length;
  }

  get totalFiltrados(): number {
    return this.mensajesFiltrados.length;
  }

  get totalPaginas(): number {
    return Math.ceil(this.mensajesFiltrados.length / this.itemsPorPagina);
  }

  get mensajesPagina(): BitacoraComunicacion[] {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    const fin = Math.min(inicio + this.itemsPorPagina, this.mensajesFiltrados.length);
    return this.mensajesFiltrados.slice(inicio, fin);
  }

  constructor(
    private bitacoraService: BitacoraService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    // Obtener rol del usuario autenticado
    this.isAdmin = this.authService.isAdmin();

    if (!this.loaded) {
      this.loaded = true;
      this.cargarMensajes();
      this.paginaActual = 1;
      this.pollingInterval = setInterval(() => {
        this.cargarMensajes(true);
      }, 30000);
    }
  }

  ngOnDestroy(): void {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
  }

  // ========== CARGAR MENSAJES ==========
  cargarMensajes(silencioso: boolean = false): void {
    if (!silencioso) {
      this.cargando = true;
    }
    this.bitacoraService.listar().subscribe({
      next: (data) => {
        this.mensajes = data
          .map(item => this.mapearMensaje(item))
          .sort((a, b) => new Date(b.fechaHora).getTime() - new Date(a.fechaHora).getTime());
        this.aplicarFiltro();
        this.paginaActual = 1;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar mensajes:', err);
        this.cargando = false;
        if (!silencioso) {
          this.mostrarError('No se pudieron cargar los mensajes. Intente de nuevo.');
        }
        this.cdr.detectChanges();
      }
    });
  }

  // ========== MAPEO ==========
  private mapearMensaje(item: any): BitacoraComunicacion {
    const fechaHora = item.fechaHora || item.fechaCreacion || item.createdAt || new Date().toISOString();
    const tipo = item.tipo || 'novedad';
    return {
      id: item.id,
      usuarioId: item.usuarioId,
      fechaHora: fechaHora,
      mensaje: item.mensaje,
      tipo: tipo,
      leido: item.leido || false,
      usuarioNombre: item.usuarioNombre || '',
      createdAt: item.createdAt || fechaHora
    };
  }

  // ========== FILTRO ==========
  cambiarFiltro(tipo: string): void {
    this.filtroTipo = tipo;
    this.aplicarFiltro();
    this.paginaActual = 1;
  }

  private aplicarFiltro(): void {
    if (this.filtroTipo === 'todos') {
      this.mensajesFiltrados = [...this.mensajes];
    } else {
      this.mensajesFiltrados = this.mensajes.filter(m => m.tipo === this.filtroTipo);
    }
    this.cdr.detectChanges();
  }

  // ========== PAGINACIÓN ==========
  irPagina(pagina: number): void {
    if (pagina < 1 || pagina > this.totalPaginas) return;
    this.paginaActual = pagina;
    this.cdr.detectChanges();
  }

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

  // ========== REFRESCAR ==========
  refrescar(): void {
    this.cargarMensajes(false);
  }

  // ========== MARCAR LEÍDO ==========
  marcarLeido(id: number): void {
    const mensaje = this.mensajes.find(m => m.id === id);
    if (!mensaje) return;

    if (mensaje.leido) {
      this.mostrarInfo('Ya está leído', 'Este mensaje ya fue marcado como leído.');
      return;
    }

    this.bitacoraService.marcarLeido(id).subscribe({
      next: () => {
        mensaje.leido = true;
        const filtrado = this.mensajesFiltrados.find(m => m.id === id);
        if (filtrado) filtrado.leido = true;
        this.mostrarExito('Marcado como leído', 'El mensaje ha sido marcado como leído correctamente.');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al marcar como leído:', err);
        this.mostrarError('No se pudo marcar el mensaje como leído.');
      }
    });
  }

  // ========== PERMISOS PARA ELIMINAR ==========
  puedeEliminar(mensaje: BitacoraComunicacion): boolean {
    // Administrador puede eliminar cualquier mensaje
    if (this.isAdmin) return true;

    // Vendedor: solo puede eliminar los mensajes que él mismo creó
    const usuarioIdStr = this.authService.getUsuarioId();
    if (usuarioIdStr) {
      const usuarioId = parseInt(usuarioIdStr, 10);
      if (mensaje.usuarioId === usuarioId) {
        return true;
      }
    }
    return false;
  }

  // ========== ELIMINAR ==========
  eliminar(id: number): void {
    const mensaje = this.mensajes.find(m => m.id === id);
    if (!mensaje) return;

    Swal.fire({
      title: '¿Eliminar mensaje?',
      html: `
        <p style="color: #475569; margin-bottom: 8px;">
          ¿Está seguro de que desea <strong style="color: #dc2626;">ELIMINAR</strong> este mensaje?
        </p>
        <div style="background: #f8fafc; border-radius: 8px; padding: 12px; border-left: 4px solid #dc2626; text-align: left;">
          <strong>${mensaje.mensaje}</strong><br>
          <span style="font-size: 0.85rem; color: #475569;">
            Tipo: ${mensaje.tipo} | Fecha: ${this.formatearFecha(mensaje.fechaHora)}
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
        this.bitacoraService.eliminar(id).subscribe({
          next: () => {
            this.mensajes = this.mensajes.filter(m => m.id !== id);
            this.mensajesFiltrados = this.mensajesFiltrados.filter(m => m.id !== id);
            if (this.mensajesPagina.length === 0 && this.paginaActual > 1) {
              this.paginaActual--;
            }
            this.mostrarExito('Mensaje eliminado', 'El mensaje ha sido eliminado correctamente.');
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error('Error al eliminar:', err);
            this.mostrarError('No se pudo eliminar el mensaje.');
          }
        });
      }
    });
  }

  // ========== MODAL ==========
  abrirModal(): void {
    this.nuevoMensaje = { mensaje: '', tipo: 'novedad' };
    this.showModal = true;
    this.cdr.detectChanges();
  }

  cerrarModal(): void {
    this.showModal = false;
    this.cdr.detectChanges();
  }

  guardarMensaje(form: NgForm): void {
    if (form.invalid) {
      this.mostrarError('Debe completar todos los campos requeridos.');
      return;
    }

    const usuarioIdStr = this.authService.getUsuarioId();
    if (!usuarioIdStr) {
      this.mostrarError('No se pudo identificar al usuario. Inicia sesión nuevamente.');
      return;
    }
    const usuarioId = parseInt(usuarioIdStr, 10);

    const request: BitacoraComunicacionRequest = {
      usuarioId: usuarioId,
      mensaje: this.nuevoMensaje.mensaje,
      tipo: this.nuevoMensaje.tipo as 'novedad' | 'recordatorio' | 'incidencia'
    };

    this.bitacoraService.crear(request).subscribe({
      next: () => {
        this.cerrarModal();
        this.mostrarExito('Mensaje creado', 'El mensaje se ha registrado correctamente.');
        this.cargarMensajes(false);
      },
      error: (err) => {
        console.error('Error al crear mensaje:', err);
        this.mostrarError('No se pudo crear el mensaje. Intente de nuevo.');
      }
    });
  }

  // ========== UTILIDADES ==========
  public formatearFecha(fechaHora: string): string {
    try {
      const fecha = new Date(fechaHora);
      return fecha.toLocaleString('es-PE', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return fechaHora || 'Fecha inválida';
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

  private mostrarInfo(titulo: string, mensaje: string): void {
    Swal.fire({
      icon: 'info',
      title: titulo,
      text: mensaje,
      confirmButtonColor: '#0d9488',
      confirmButtonText: 'Entendido',
      customClass: { popup: 'swal-farmaceutico' }
    });
  }
}