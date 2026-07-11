import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../../core/services/cliente';
import { VentaService } from '../../../core/services/venta';
import { AuthService } from '../../../core/auth';
import { Cliente } from '../../../core/models/cliente.model';
import { Venta } from '../../../core/models/venta.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-listar-clientes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  // ======== DATOS ========
  clientes: Cliente[] = [];
  clientesFiltrados: Cliente[] = [];
  clienteSeleccionado: Cliente | null = null;
  historialVentas: Venta[] = [];

  // ======== FILTRO ========
  filtroTexto: string = '';

  // ======== ESTADOS ========
  cargando: boolean = false;
  cargandoHistorial: boolean = false;

  // ======== PERMISOS ========
  isAdmin: boolean = false;

  constructor(
    private clienteService: ClienteService,
    private ventaService: VentaService,
    public auth: AuthService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) { }

  ngOnInit(): void {
    this.isAdmin = this.auth.isAdmin();
    this.cargarClientes();
  }

  // ======== CARGA DE CLIENTES ========
  cargarClientes(): void {
    this.cargando = true;
    this.clienteService.listar().subscribe({
      next: (data: Cliente[]) => {
        this.clientes = data;
        this.aplicarFiltro();
        this.cargando = false;
        this.ngZone.run(() => this.cdr.detectChanges());
      },
      error: (err: any) => {
        console.error('Error al cargar clientes:', err);
        this.cargando = false;
        this.mostrarError('No se pudieron cargar los clientes.');
      }
    });
  }

  // ======== FILTRO ========
  aplicarFiltro(): void {
    if (!this.filtroTexto.trim()) {
      this.clientesFiltrados = [...this.clientes];
      return;
    }
    const texto = this.filtroTexto.toLowerCase().trim();
    this.clientesFiltrados = this.clientes.filter(c =>
      c.nombre.toLowerCase().includes(texto) ||
      c.documentoNumero.includes(texto) ||
      c.documentoTipo.toLowerCase().includes(texto) ||
      (c.telefono && c.telefono.includes(texto)) ||
      (c.email && c.email.toLowerCase().includes(texto))
    );
    this.ngZone.run(() => this.cdr.detectChanges());
  }

  limpiarFiltro(): void {
    this.filtroTexto = '';
    this.aplicarFiltro();
  }

  // ======== SELECCIONAR CLIENTE (CON CARGA INSTANTÁNEA) ========
  seleccionarCliente(cliente: Cliente): void {
    this.clienteSeleccionado = cliente;
    // Forzar limpieza y recarga del historial
    this.historialVentas = [];
    this.cargarHistorial(cliente);
  }

  // ======== CARGAR HISTORIAL (INSTANTÁNEO) ========
  cargarHistorial(cliente: Cliente): void {
    this.cargandoHistorial = true;
    this.historialVentas = [];
    this.ngZone.run(() => this.cdr.detectChanges());

    this.ventaService.listarPorCliente(cliente.id!).subscribe({
      next: (ventas: Venta[]) => {
        this.historialVentas = ventas.sort((a, b) =>
          new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
        );
        this.cargandoHistorial = false;
        // Forzar detección de cambios inmediatamente después de asignar los datos
        this.ngZone.run(() => this.cdr.detectChanges());
      },
      error: (err: any) => {
        console.error('Error al cargar historial:', err);
        this.cargandoHistorial = false;
        this.mostrarError('No se pudo cargar el historial del cliente.');
        this.ngZone.run(() => this.cdr.detectChanges());
      }
    });
  }

  // ======== ELIMINAR CLIENTE (SOLO ADMIN, ACTUALIZACIÓN INSTANTÁNEA) ========
  eliminarCliente(cliente: Cliente): void {
    if (!this.isAdmin) return;

    Swal.fire({
      title: '¿Eliminar cliente?',
      html: `
        <p style="color: #475569; margin-bottom: 8px;">
          ¿Está seguro de que desea <strong style="color: #dc2626;">BORRAR</strong> permanentemente al cliente?
        </p>
        <div class="accion-resumen">
          <strong>${cliente.nombre}</strong><br>
          DNI: ${cliente.documentoNumero} | Tel: ${cliente.telefono || 'N/A'}
        </div>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#0d9488',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, borrar',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      customClass: {
        popup: 'swal-farmaceutico',
        confirmButton: 'swal2-confirm',
        cancelButton: 'swal2-cancel'
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.clienteService.eliminar(cliente.id!).subscribe({
          next: () => {
            // 🔥 Actualización INSTANTÁNEA: se quita el cliente de la lista en memoria,
            // sin esperar un nuevo round-trip completo al backend (cargarClientes()).
            this.clientes = this.clientes.filter(c => c.id !== cliente.id);
            this.aplicarFiltro();

            // Limpiar historial si era el cliente seleccionado
            if (this.clienteSeleccionado?.id === cliente.id) {
              this.clienteSeleccionado = null;
              this.historialVentas = [];
            }

            this.ngZone.run(() => this.cdr.detectChanges());
            this.mostrarExito('Cliente eliminado', `"${cliente.nombre}" ha sido eliminado correctamente.`);
          },
          error: (err: any) => {
            console.error('Error al eliminar cliente:', err);
            this.mostrarError(err.error?.mensaje || 'No se pudo eliminar el cliente.');
          }
        });
      }
    });
  }

  // ======== LIMPIAR HISTORIAL (SOLO ADMIN) ========
  limpiarHistorial(): void {
    if (!this.isAdmin || !this.clienteSeleccionado) {
      this.mostrarError('No tienes permiso o no hay cliente seleccionado.');
      return;
    }

    Swal.fire({
      title: '¿Limpiar historial?',
      html: `
        <p style="color: #475569; margin-bottom: 8px;">
          ¿Está seguro de que desea <strong style="color: #dc2626;">ELIMINAR</strong> todo el historial de compras?
        </p>
        <div class="accion-resumen">
          <strong>${this.clienteSeleccionado.nombre}</strong><br>
          Total de compras: ${this.historialVentas.length} registros
        </div>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#0d9488',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, limpiar',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      customClass: {
        popup: 'swal-farmaceutico',
        confirmButton: 'swal2-confirm',
        cancelButton: 'swal2-cancel'
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.ventaService.limpiarHistorialCliente(this.clienteSeleccionado!.id!).subscribe({
          next: () => {
            this.historialVentas = [];
            this.mostrarExito(
              'Historial limpiado',
              `El historial de compras de "${this.clienteSeleccionado!.nombre}" ha sido eliminado correctamente.`
            );
            this.clienteSeleccionado = null;
            this.ngZone.run(() => this.cdr.detectChanges());
          },
          error: (err: any) => {
            console.error('Error al limpiar historial:', err);
            this.mostrarError(err.error?.mensaje || 'No se pudo limpiar el historial.');
          }
        });
      }
    });
  }

  // ======== OBTENER COLOR PARA AVATAR ========
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

  // ======== MÉTODOS DE MENSAJES FARMACÉUTICOS ========
  private mostrarExito(titulo: string, mensaje: string): void {
    Swal.fire({
      icon: 'success',
      title: titulo,
      text: mensaje,
      timer: 3000,
      showConfirmButton: false,
      customClass: {
        popup: 'swal-farmaceutico'
      }
    });
  }

  private mostrarError(mensaje: string): void {
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: mensaje,
      confirmButtonColor: '#0d9488',
      confirmButtonText: 'Aceptar',
      customClass: {
        popup: 'swal-farmaceutico',
        confirmButton: 'btn-farma-confirm'
      }
    });
  }
}