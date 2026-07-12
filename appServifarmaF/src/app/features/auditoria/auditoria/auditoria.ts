import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditoriaService} from '../../../core/services/auditoria';
import { ResumenActividad } from '../../../core/models/auditoria.model';
import { UsuarioService } from '../../../core/services/usuario';
import { Usuario } from '../../../core/models/usuario.model';
import { AuthService } from '../../../core/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auditoria.html',
  styleUrls: ['./auditoria.css']
})
export class AuditoriaComponent implements OnInit {
  // ========== MATH para la plantilla ==========
  public Math = Math;

  // ========== FILTROS ==========
  usuarioFiltro: string = 'todos';
  accionFiltro: string = '';
  fechaInicio: string = '';
  fechaFin: string = '';

  // ========== DATOS ==========
  registros: any[] = [];
  registrosFiltrados: any[] = [];
  resumen: ResumenActividad | null = null;

  // ========== PAGINACIÓN ==========
  itemsPorPagina: number = 10;
  paginaActual: number = 1;

  // ========== ESTADOS ==========
  cargando: boolean = false;
  hayDatos: boolean = false;

  // ========== OPCIONES (usuarios reales) ==========
  usuarios: { id: number; nombre: string }[] = [];

  constructor(
    private auditoriaService: AuditoriaService,
    private usuarioService: UsuarioService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.establecerFechasPorDefecto();
    this.cargarUsuarios();
    this.cargarDatos();
  }

  // ========== FECHAS ==========
  establecerFechasPorDefecto(): void {
    const hoy = new Date();
    const primerDia = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
    this.fechaInicio = this.formatDate(primerDia);
    this.fechaFin = this.formatDate(ultimoDia);
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  // ========== CARGAR USUARIOS REALES DESDE EL BACKEND ==========
  cargarUsuarios(): void {
    this.usuarioService.listar().subscribe({
      next: (data: Usuario[]) => {
        this.usuarios = data.map(u => ({
          id: u.id!,
          nombre: u.nombreCompleto || u.usuario
        }));
        // Ordenar alfabéticamente
        this.usuarios.sort((a, b) => a.nombre.localeCompare(b.nombre));
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar usuarios:', err);
        // Fallback: usar el usuario autenticado si falla la carga
        this.cargarUsuarioAutenticado();
      }
    });
  }

  // ========== FALLBACK: Usuario autenticado ==========
  private cargarUsuarioAutenticado(): void {
    try {
      const usuarioId = this.authService.getUsuarioId();
      const nombre = this.authService.getNombreCompleto() || 'Usuario actual';
      if (usuarioId) {
        this.usuarios = [{ id: parseInt(usuarioId, 10), nombre }];
      }
    } catch (e) {
      console.warn('No se pudo obtener usuario autenticado:', e);
    }
  }

  // ========== CARGAR DATOS DE AUDITORÍA ==========
  cargarDatos(): void {
    if (!this.fechaInicio || !this.fechaFin) {
      Swal.fire('Error', 'Selecciona ambas fechas', 'error');
      return;
    }

    this.cargando = true;
    const inicio = this.fechaInicio + 'T00:00:00';
    const fin = this.fechaFin + 'T23:59:59';

    Promise.all([
      this.cargarResumen(inicio, fin),
      this.cargarRegistros(inicio, fin)
    ]).finally(() => {
      this.cargando = false;
      this.cdr.detectChanges();
    });
  }

  private cargarResumen(inicio: string, fin: string): Promise<void> {
    return new Promise((resolve) => {
      this.auditoriaService.obtenerResumenActividad(inicio, fin).subscribe({
        next: (data) => {
          this.resumen = data;
          resolve();
        },
        error: (err) => {
          console.error('Error al cargar resumen:', err);
          resolve();
        }
      });
    });
  }

  private cargarRegistros(inicio: string, fin: string): Promise<void> {
    return new Promise((resolve) => {
      // Combinar ventas anuladas y movimientos de stock
      Promise.all([
        this.auditoriaService.obtenerVentasAnuladas(inicio, fin).toPromise(),
        this.auditoriaService.obtenerMovimientosPorTipo('venta', inicio, fin).toPromise(),
        this.auditoriaService.obtenerMovimientosPorTipo('merma', inicio, fin).toPromise()
      ]).then(([ventasAnuladas, ventasStock, mermas]) => {
        const registros: any[] = [];

        // Mapear ventas anuladas
        (ventasAnuladas || []).forEach((v: any) => {
          const usuarioNombre = this.getNombreUsuario(v.usuarioId);
          registros.push({
            id: v.id,
            usuario: usuarioNombre || 'Usuario #' + v.usuarioId,
            usuarioId: v.usuarioId,
            accion: 'Venta anulada',
            fecha: v.fecha?.split('T')[0] || '',
            hora: v.fecha?.split('T')[1]?.substring(0, 8) || '',
            modulo: 'Ventas',
            detalle: `Total: S/ ${v.total?.toFixed(2) || '0.00'}`
          });
        });

        // Mapear movimientos de stock (ventas)
        (ventasStock || []).forEach((m: any) => {
          const usuarioNombre = this.getNombreUsuario(m.usuarioId);
          registros.push({
            id: m.id,
            usuario: usuarioNombre || 'Usuario #' + m.usuarioId,
            usuarioId: m.usuarioId,
            accion: 'Movimiento de stock (venta)',
            fecha: m.fecha?.split('T')[0] || '',
            hora: m.fecha?.split('T')[1]?.substring(0, 8) || '',
            modulo: 'Inventario',
            detalle: `Cantidad: ${m.cantidad} | Costo unitario: S/ ${m.costoUnitario?.toFixed(2) || '0.00'}`
          });
        });

        // Mapear mermas
        (mermas || []).forEach((m: any) => {
          const usuarioNombre = this.getNombreUsuario(m.usuarioId);
          const perdida = (m.costoUnitario || 0) * (m.cantidad || 0);
          registros.push({
            id: m.id,
            usuario: usuarioNombre || 'Usuario #' + m.usuarioId,
            usuarioId: m.usuarioId,
            accion: 'Merma registrada',
            fecha: m.fecha?.split('T')[0] || '',
            hora: m.fecha?.split('T')[1]?.substring(0, 8) || '',
            modulo: 'Inventario',
            detalle: `Cantidad: ${m.cantidad} | Pérdida: S/ ${perdida.toFixed(2)}`
          });
        });

        // Ordenar por fecha descendente
        registros.sort((a, b) => {
          const fechaA = new Date(a.fecha + 'T' + a.hora);
          const fechaB = new Date(b.fecha + 'T' + b.hora);
          return fechaB.getTime() - fechaA.getTime();
        });

        this.registros = registros;
        this.aplicarFiltros();
        this.paginaActual = 1;
        this.hayDatos = this.registros.length > 0;
        resolve();
      }).catch(err => {
        console.error('Error al cargar registros:', err);
        this.registros = [];
        this.aplicarFiltros();
        resolve();
      });
    });
  }

  // ========== OBTENER NOMBRE DE USUARIO DESDE LA LISTA REAL ==========
  private getNombreUsuario(usuarioId: number): string | null {
    const found = this.usuarios.find(u => u.id === usuarioId);
    return found ? found.nombre : null;
  }

  // ========== FILTROS ==========
  aplicarFiltros(): void {
    let filtrados = [...this.registros];

    if (this.usuarioFiltro !== 'todos') {
      const usuarioId = parseInt(this.usuarioFiltro, 10);
      filtrados = filtrados.filter(r => r.usuarioId === usuarioId);
    }

    if (this.accionFiltro.trim()) {
      const busqueda = this.accionFiltro.toLowerCase().trim();
      filtrados = filtrados.filter(r =>
        r.accion.toLowerCase().includes(busqueda) ||
        r.modulo.toLowerCase().includes(busqueda) ||
        r.usuario.toLowerCase().includes(busqueda)
      );
    }

    this.registrosFiltrados = filtrados;
    this.cdr.detectChanges();
  }

  // ========== PAGINACIÓN ==========
  get totalPaginas(): number {
    return Math.ceil(this.registrosFiltrados.length / this.itemsPorPagina);
  }

  get registrosPagina(): any[] {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    const fin = Math.min(inicio + this.itemsPorPagina, this.registrosFiltrados.length);
    return this.registrosFiltrados.slice(inicio, fin);
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
}