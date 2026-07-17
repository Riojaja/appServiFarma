import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { Subject, finalize, takeUntil } from 'rxjs';
import { UsuarioService } from '../../../core/services/usuario';
import { AuthService } from '../../../core/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-listar-usuarios',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarUsuariosComponent implements OnInit, OnDestroy {
  usuarios: any[] = [];
  usuariosFiltrados: any[] = [];
  cargando: boolean = false;
  isAdmin: boolean = false;
  filtroBusqueda: string = '';
  usuarioActualId: number | null = null;

  procesandoId: number | null = null;
  ejecutandoCierreTurno: boolean = false;
  private destroy$ = new Subject<void>();

  showModal: boolean = false;
  esEdicion: boolean = false;
  cargandoModal: boolean = false;
  usuarioId: number | null = null;

  usuarioForm = {
    nombreCompleto: '',
    usuario: '',
    contrasena: '',
    rolId: 2, // 2 = Vendedor por defecto (para el formulario)
    activo: true
  };

  roles = [
    { id: 1, nombre: 'Administrador' },
    { id: 2, nombre: 'Vendedor' }
  ];

  rolesPermisos = [
    {
      nombre: 'Administrador',
      color: 'admin',
      permisos: [
        'Acceso completo al sistema',
        'Gestión de usuarios',
        'Reportes financieros',
        'Configuración del sistema'
      ]
    },
    {
      nombre: 'Vendedor',
      color: 'vendedor',
      permisos: [
        'Registro de ventas',
        'Consulta de inventario',
        'Gestión de clientes',
        'Sin acceso a reportes financieros'
      ]
    }
  ];

  constructor(
    private usuarioService: UsuarioService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    const id = this.authService.getUsuarioId();
    this.usuarioActualId = id ? Number(id) : null;
    this.cargarUsuarios();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get hayOperacionEnCurso(): boolean {
    return this.cargando || this.procesandoId !== null || this.ejecutandoCierreTurno;
  }

  estaBloqueado(usuarioId: number): boolean {
    return this.procesandoId === usuarioId || this.cargando || this.ejecutandoCierreTurno;
  }

  // ======== FUNCIONES PARA OBTENER DATOS DEL ROL ========

  /** Obtiene el nombre del rol de un usuario, soportando diferentes estructuras */
  getRolNombre(usuario: any): string {
    if (!usuario) return '—';
    // Prioridad: rol.nombre, luego si es string directo
    if (usuario.rol?.nombre) return usuario.rol.nombre;
    if (typeof usuario.rol === 'string') return usuario.rol;
    if (usuario.rolNombre) return usuario.rolNombre;
    // Si solo tiene rol.id, buscar en la lista de roles
    if (usuario.rol?.id) {
      const encontrado = this.roles.find(r => r.id === usuario.rol.id);
      return encontrado ? encontrado.nombre : '—';
    }
    return '—';
  }

  /** Devuelve la clase CSS para el badge según el rol */
  getRolBadgeClass(usuario: any): string {
    const nombre = this.getRolNombre(usuario);
    return nombre === 'Administrador' ? 'badge-rol-admin' : 'badge-rol-vendedor';
  }

  /** Verifica si un usuario es administrador */
  esAdministrador(usuario: any): boolean {
    return this.getRolNombre(usuario) === 'Administrador';
  }

  // ======== CARGA DE DATOS ========

  cargarUsuarios(): void {
    if (this.cargando) return;
    this.cargando = true;
    this.usuarioService.listar()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (data) => {
          this.usuarios = data;
          console.log('📦 Usuarios recibidos:', this.usuarios);
          this.aplicarFiltro();
          this.cdr.detectChanges();
        },
        error: () => {
          Swal.fire({
            title: 'Error',
            text: 'No se pudieron cargar los usuarios',
            icon: 'error',
            customClass: { popup: 'swal-farmaceutico' }
          });
          this.cdr.detectChanges();
        }
      });
  }

  aplicarFiltro(): void {
    if (!this.filtroBusqueda.trim()) {
      this.usuariosFiltrados = [...this.usuarios];
      return;
    }
    const texto = this.filtroBusqueda.toLowerCase().trim();
    this.usuariosFiltrados = this.usuarios.filter(u =>
      u.nombreCompleto.toLowerCase().includes(texto) ||
      u.usuario.toLowerCase().includes(texto)
    );
  }

  obtenerColorAvatar(nombre: string): string {
    const colores = ['#0d9488', '#3b82f6', '#8b5cf6', '#f59e0b', '#ec4899', '#14b8a6', '#f97316', '#6366f1'];
    const texto = nombre || '';
    const index = Math.abs(texto.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)) % colores.length;
    return colores[index];
  }

  // ======== MODAL ========

  abrirModalCrear(): void {
    if (this.cargando) return;
    this.esEdicion = false;
    this.usuarioId = null;
    this.usuarioForm = {
      nombreCompleto: '',
      usuario: '',
      contrasena: '',
      rolId: 2,
      activo: true
    };
    this.showModal = true;
    this.cdr.detectChanges();
  }

  abrirModalEditar(id: number): void {
    if (this.cargando || this.procesandoId !== null) return;
    this.esEdicion = true;
    this.usuarioId = id;
    this.cargandoModal = true;
    this.showModal = true;
    this.cdr.detectChanges();

    this.usuarioService.obtener(id)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.cargandoModal = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (data) => {
          // ✅ CORREGIDO: usar data.rol?.id en lugar de data.rolId
          this.usuarioForm = {
            nombreCompleto: data.nombreCompleto || '',
            usuario: data.usuario || '',
            contrasena: '',
            rolId: data.rol?.id || 2,
            activo: data.activo !== undefined ? data.activo : true
          };
          this.cdr.detectChanges();
        },
        error: () => {
          Swal.fire({
            title: 'Error',
            text: 'No se pudo cargar el usuario',
            icon: 'error',
            customClass: { popup: 'swal-farmaceutico' }
          });
          this.cerrarModal();
        }
      });
  }

  cerrarModal(): void {
    this.showModal = false;
    this.cdr.detectChanges();
  }

  guardarUsuario(form: NgForm): void {
    if (form.invalid) {
      Swal.fire({
        title: 'Error',
        text: 'Por favor complete todos los campos requeridos',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    if (!this.esEdicion && (!this.usuarioForm.contrasena || this.usuarioForm.contrasena.length < 6)) {
      Swal.fire({
        title: 'Error',
        text: 'La contraseña debe tener al menos 6 caracteres',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    this.cargandoModal = true;
    const data = {
      nombreCompleto: this.usuarioForm.nombreCompleto,
      usuario: this.usuarioForm.usuario,
      contrasena: this.usuarioForm.contrasena,
      rolId: this.usuarioForm.rolId,
      activo: this.usuarioForm.activo
    };

    const operation = this.esEdicion
      ? this.usuarioService.actualizar(this.usuarioId!, data)
      : this.usuarioService.crear(data);

    operation
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.cargandoModal = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          Swal.fire({
            title: 'Éxito',
            text: this.esEdicion ? 'Usuario actualizado correctamente' : 'Usuario creado correctamente',
            icon: 'success',
            timer: 2000,
            showConfirmButton: false,
            customClass: { popup: 'swal-farmaceutico' }
          });
          this.cerrarModal();
          this.cargarUsuarios();
        },
        error: (err) => {
          const mensaje = err?.error?.mensaje || err?.error?.message || 'Error al guardar el usuario';
          Swal.fire({
            title: 'Error',
            text: mensaje,
            icon: 'error',
            customClass: { popup: 'swal-farmaceutico' }
          });
        }
      });
  }

  // ======== ACCIONES SOBRE USUARIOS ========

  cambiarEstado(id: number, activo: boolean): void {
    if (this.procesandoId !== null) return;

    const usuario = this.usuarios.find(u => u.id === id);
    if (!usuario) return;

    // 🔒 No permitir cambiar estado de administradores
    if (this.esAdministrador(usuario)) {
      Swal.fire({
        title: 'Operación no permitida',
        text: 'No se puede cambiar el estado de un administrador.',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    if (this.usuarioActualId === id) {
      Swal.fire({
        title: 'Operación no permitida',
        text: 'No puedes cambiar tu propio estado.',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    const mensaje = activo ? 'activar' : 'desactivar';
    Swal.fire({
      title: `¿${mensaje.charAt(0).toUpperCase() + mensaje.slice(1)} usuario?`,
      text: `¿Está seguro de ${mensaje} a "${usuario.nombreCompleto}"?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#0d9488',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (result.isConfirmed) {
        this.procesandoId = id;
        this.usuarioService.cambiarEstado(id, activo)
          .pipe(
            takeUntil(this.destroy$),
            finalize(() => {
              this.procesandoId = null;
              this.cdr.detectChanges();
            })
          )
          .subscribe({
            next: () => {
              const usuarioActualizado = this.usuarios.find(u => u.id === id);
              if (usuarioActualizado) usuarioActualizado.activo = activo;
              this.aplicarFiltro();
              this.cdr.detectChanges();
              Swal.fire({
                title: '✅ Éxito',
                text: `Usuario ${activo ? 'activado' : 'desactivado'} correctamente`,
                icon: 'success',
                timer: 2200,
                showConfirmButton: false,
                customClass: { popup: 'swal-farmaceutico' }
              });
            },
            error: (err) => {
              // Mostrar el mensaje real del backend
              const mensajeError = err?.error?.mensaje || err?.error?.message || 'No se pudo cambiar el estado.';
              Swal.fire({
                title: '❌ Error',
                text: mensajeError,
                icon: 'error',
                customClass: { popup: 'swal-farmaceutico' }
              });
              // Recargar la lista para asegurar consistencia
              this.cargarUsuarios();
            }
          });
      }
    });
  }

  cerrarSesiones(id: number): void {
    if (this.procesandoId !== null) return;

    Swal.fire({
      title: '¿Cerrar sesiones?',
      text: 'Esto cerrará todas las sesiones activas de este usuario.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#0d9488',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, cerrar',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (result.isConfirmed) {
        this.procesandoId = id;
        this.usuarioService.cerrarSesiones(id)
          .pipe(
            takeUntil(this.destroy$),
            finalize(() => {
              this.procesandoId = null;
              this.cdr.detectChanges();
            })
          )
          .subscribe({
            next: () => {
              Swal.fire({
                title: 'Éxito',
                text: 'Sesiones cerradas correctamente',
                icon: 'success',
                timer: 2200,
                showConfirmButton: false,
                customClass: { popup: 'swal-farmaceutico' }
              });
            },
            error: () => {
              Swal.fire({
                title: 'Error',
                text: 'No se pudieron cerrar las sesiones',
                icon: 'error',
                customClass: { popup: 'swal-farmaceutico' }
              });
            }
          });
      }
    });
  }

  eliminarUsuario(usuario: any): void {
    if (this.procesandoId !== null) return;

    // No permitir eliminar administradores
    if (this.esAdministrador(usuario)) {
      Swal.fire({
        title: 'Operación no permitida',
        text: 'No se puede eliminar a un administrador.',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    Swal.fire({
      title: '¿Eliminar usuario?',
      html: `
        <p style="color:#475569;margin-bottom:8px;">
          ¿Está seguro de que desea <strong style="color:#dc2626;">ELIMINAR</strong> permanentemente a este usuario?
        </p>
        <div style="background:#f8fafc;border-radius:8px;padding:12px;border-left:4px solid #dc2626;text-align:left;">
          <strong>${usuario.nombreCompleto}</strong><br>
          <span style="font-size:0.85rem;color:#475569;">Usuario: ${usuario.usuario} · Rol: ${this.getRolNombre(usuario)}</span>
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
        this.procesandoId = usuario.id;
        this.usuarioService.eliminar(usuario.id)
          .pipe(
            takeUntil(this.destroy$),
            finalize(() => {
              this.procesandoId = null;
              this.cdr.detectChanges();
            })
          )
          .subscribe({
            next: () => {
              this.usuarios = this.usuarios.filter(u => u.id !== usuario.id);
              this.aplicarFiltro();
              this.cdr.detectChanges();
              Swal.fire({
                title: 'Eliminado',
                text: 'El usuario ha sido eliminado correctamente.',
                icon: 'success',
                timer: 2200,
                showConfirmButton: false,
                customClass: { popup: 'swal-farmaceutico' }
              });
            },
            error: (err) => {
              const mensaje = err?.error?.mensaje || err?.error?.message || 'No se pudo eliminar el usuario.';
              Swal.fire({
                title: 'Error',
                text: mensaje,
                icon: 'error',
                customClass: { popup: 'swal-farmaceutico' }
              });
            }
          });
      }
    });
  }

  ejecutarCierreTurno(): void {
    if (this.ejecutandoCierreTurno) return;

    Swal.fire({
      title: '¿Ejecutar cierre de turno?',
      text: 'Esto cerrará todas las sesiones de vendedores activos.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#0d9488',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Ejecutar',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (result.isConfirmed) {
        this.ejecutandoCierreTurno = true;
        this.usuarioService.cerrarSesionesTurno()
          .pipe(
            takeUntil(this.destroy$),
            finalize(() => {
              this.ejecutandoCierreTurno = false;
              this.cdr.detectChanges();
            })
          )
          .subscribe({
            next: () => {
              Swal.fire({
                title: 'Éxito',
                text: 'Cierre de turno ejecutado',
                icon: 'success',
                timer: 2200,
                showConfirmButton: false,
                customClass: { popup: 'swal-farmaceutico' }
              });
            },
            error: () => {
              Swal.fire({
                title: 'Error',
                text: 'No se pudo ejecutar el cierre de turno',
                icon: 'error',
                customClass: { popup: 'swal-farmaceutico' }
              });
            }
          });
      }
    });
  }

  cerrarSesion(): void {
    Swal.fire({
      title: '¿Cerrar sesión?',
      text: '¿Estás seguro de que deseas salir del sistema?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, cerrar sesión',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (result.isConfirmed) {
        this.authService.logout();
        this.router.navigate(['/login']);
      }
    });
  }

  trackByUsuarioId(index: number, usuario: any): number {
    return usuario.id;
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.cerrarModal();
    }
  }
}