import { Component, OnInit, OnDestroy, ChangeDetectorRef, HostListener, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LoteService } from '../../../core/services/lote';
import { ProductoService } from '../../../core/services/producto';
import { ProveedorService } from '../../../core/services/proveedor';
import { AuthService } from '../../../core/auth';
import { Lote } from '../../../core/models/lote.model';
import { Producto } from '../../../core/models/producto.model';
import { Proveedor } from '../../../core/models/proveedor.model';
import { environment } from '../../../../environments/environment';
import Swal from 'sweetalert2';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-listar-lotes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit, OnDestroy {
  // ======== DATOS PRINCIPALES ========
  lotes: Lote[] = [];
  lotesFiltrados: Lote[] = [];
  lotesMostrados: Lote[] = [];

  // ======== FILTROS ========
  filtroProducto: string = '';
  filtroEstado: string = '';
  filtroVencimiento: string = '';
  fechaIngresoActual: string = '';
  fechaIngresoISO: string = '';

  // ======== ESTADOS ========
  cargando: boolean = false;
  isAdmin: boolean = false;
  private destroy$ = new Subject<void>();

  // ======== PAGINACIÓN (SCROLL INFINITO) ========
  itemsPorPagina: number = 8;
  paginaActual: number = 1;
  cargandoMas: boolean = false;
  todosCargados: boolean = false;

  // ======== ALERTAS ========
  stockBajo: Lote[] = [];
  proximoVencer: Lote[] = [];

  // ======== OFFCANVAS CREAR ========
  offcanvasCrearAbierto: boolean = false;
  guardando: boolean = false;
  formCrear: FormGroup;
  productos: Producto[] = [];
  proveedores: Proveedor[] = [];

  // ======== MODALES DE SELECCIÓN ========
  modalProductosAbierto: boolean = false;
  modalProveedoresAbierto: boolean = false;
  productosModal: Producto[] = [];
  proveedoresModal: Proveedor[] = [];
  filtroProductoModal: string = '';
  filtroProveedorModal: string = '';
  cargandoProductosModal: boolean = false;
  cargandoProveedoresModal: boolean = false;

  // ======== MODAL EDITAR ========
  modalEditarAbierto: boolean = false;
  guardandoEditar: boolean = false;
  formEditar: FormGroup;
  loteIdEditar!: number;

  // ======== MODAL AJUSTAR STOCK ========
  modalAjustarAbierto: boolean = false;
  guardandoAjustar: boolean = false;
  formAjustar: FormGroup;
  loteAjustar: Lote | null = null;
  loteIdAjustar!: number;
  usuarioId: number = 0;

  // ======== MODAL DETALLE ========
  modalDetalleAbierto: boolean = false;
  loteDetalle: Lote | null = null;

  // ======== REFERENCIAS A ELEMENTOS (para scroll infinito en modales) ========
  @ViewChild('productosScroll') productosScroll!: ElementRef;
  @ViewChild('proveedoresScroll') proveedoresScroll!: ElementRef;

  constructor(
    private loteService: LoteService,
    private productoService: ProductoService,
    private proveedorService: ProveedorService,
    private authService: AuthService,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    // ===== FORMULARIO CREAR (sin fechaIngreso, se asigna automáticamente) =====
    this.formCrear = this.fb.group({
      productoId: [null, Validators.required],
      proveedorId: [null, Validators.required],
      lote: ['', [Validators.required, Validators.maxLength(50)]],
      fechaVencimiento: ['', Validators.required],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      precioCompra: [0, [Validators.required, Validators.min(0.01)]],
      precioVenta: [0, [Validators.required, Validators.min(0.01)]],
      usuarioId: [null, Validators.required]
    });

    // ===== FORMULARIO EDITAR =====
    this.formEditar = this.fb.group({
      productoId: [null, Validators.required],
      proveedorId: [null, Validators.required],
      lote: ['', [Validators.required, Validators.maxLength(50)]],
      fechaIngreso: ['', Validators.required],
      fechaVencimiento: ['', Validators.required],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      precioCompra: [0, [Validators.required, Validators.min(0.01)]],
      precioVenta: [0, [Validators.required, Validators.min(0.01)]]
    });

    // ===== FORMULARIO AJUSTAR STOCK =====
    this.formAjustar = this.fb.group({
      tipoMovimiento: ['ajuste', Validators.required],
      cantidad: ['', [Validators.required, Validators.min(1)]],
      observacion: ['']
    });
  }

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
    this.formCrear.patchValue({ usuarioId: this.usuarioId });

    // Calcular fecha actual en formato local
    this.actualizarFechas();

    this.cargarProductos();
    this.cargarProveedores();

    this.route.url.pipe(takeUntil(this.destroy$)).subscribe(url => {
      const path = url.map(seg => seg.path).join('/');
      this.filtroVencimiento = path.includes('proximos-a-vencer') ? 'proximo' : '';
      this.cargarLotes();
    });

    this.cdr.detectChanges();
  }

  // Método auxiliar para actualizar fechas
  private actualizarFechas(): void {
    const hoy = new Date();
    const dd = String(hoy.getDate()).padStart(2, '0');
    const mm = String(hoy.getMonth() + 1).padStart(2, '0');
    const yyyy = hoy.getFullYear();
    this.fechaIngresoActual = `${dd}/${mm}/${yyyy}`;
    
    // Para el backend: fecha en formato YYYY-MM-DD (zona horaria local)
    const year = hoy.getFullYear();
    const month = String(hoy.getMonth() + 1).padStart(2, '0');
    const day = String(hoy.getDate()).padStart(2, '0');
    this.fechaIngresoISO = `${year}-${month}-${day}`;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ============================================================
  // CARGA DE DATOS
  // ============================================================

  cargarLotes(): void {
    if (this.cargando) return;
    this.cargando = true;

    this.loteService.listar()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: Lote[]) => {
          let filtrados = data;
          if (this.filtroVencimiento === 'proximo') {
            const hoy = new Date();
            hoy.setHours(0, 0, 0, 0);
            const limite = new Date(hoy);
            limite.setDate(limite.getDate() + 30);
            filtrados = data.filter(l =>
              l.estado === 'activo' &&
              new Date(l.fechaVencimiento) >= hoy &&
              new Date(l.fechaVencimiento) <= limite
            );
          }
          this.lotes = filtrados;
          this.aplicarFiltros();
          this.calcularAlertas();
          this.cargando = false;
          this.cdr.detectChanges();
          setTimeout(() => this.cdr.detectChanges(), 50);
        },
        error: (err) => {
          console.error(err);
          this.cargando = false;
          Swal.fire('Error', 'No se pudieron cargar los lotes', 'error');
          this.cdr.detectChanges();
        }
      });
  }

  cargarProductos(): void {
    this.productoService.listar()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => this.productos = data,
        error: (err) => console.error('Error cargando productos:', err)
      });
  }

  cargarProveedores(): void {
    this.proveedorService.listar()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => this.proveedores = data,
        error: (err) => console.error('Error cargando proveedores:', err)
      });
  }

  // ============================================================
  // FILTROS Y BÚSQUEDA
  // ============================================================

  aplicarFiltros(): void {
    let filtrados = this.lotes;
    if (this.filtroEstado) {
      filtrados = filtrados.filter(l => l.estado === this.filtroEstado);
    }
    this.lotesFiltrados = filtrados;
    this.paginaActual = 1;
    this.todosCargados = false;
    this.cargarMasLotes();
    this.cdr.detectChanges();
  }

  buscar(): void {
    if (this.filtroProducto.trim()) {
      const id = Number(this.filtroProducto);
      if (!isNaN(id)) {
        this.loteService.listarPorProducto(id)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (data) => {
              this.lotes = data;
              this.aplicarFiltros();
              this.cdr.detectChanges();
            },
            error: (err) => console.error(err)
          });
      } else {
        this.loteService.buscarPorLote(this.filtroProducto)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (data) => {
              this.lotes = [data];
              this.aplicarFiltros();
              this.cdr.detectChanges();
            },
            error: (err) => {
              if (err.status === 404) {
                this.lotes = [];
                this.aplicarFiltros();
                this.cdr.detectChanges();
              } else {
                console.error(err);
              }
            }
          });
      }
    } else {
      this.cargarLotes();
    }
  }

  limpiarFiltros(): void {
    this.filtroProducto = '';
    this.filtroEstado = '';
    this.router.navigate(['/inventario']);
    this.cargarLotes();
  }

  // ============================================================
  // SCROLL INFINITO
  // ============================================================

  cargarMasLotes(): void {
    if (this.todosCargados || this.cargandoMas) return;
    const fin = this.paginaActual * this.itemsPorPagina;
    this.lotesMostrados = this.lotesFiltrados.slice(0, fin);
    if (fin >= this.lotesFiltrados.length) {
      this.todosCargados = true;
    }
    this.cdr.detectChanges();
  }

  @HostListener('window:scroll')
  onScroll(): void {
    if (this.cargando || this.todosCargados || this.cargandoMas || this.lotesFiltrados.length === 0) return;
    const scrollTop = window.scrollY || document.documentElement.scrollTop;
    const windowHeight = window.innerHeight;
    const documentHeight = document.documentElement.scrollHeight;
    if (scrollTop + windowHeight >= documentHeight - 150) {
      this.cargandoMas = true;
      setTimeout(() => {
        this.paginaActual++;
        this.cargarMasLotes();
        this.cargandoMas = false;
        this.cdr.detectChanges();
      }, 300);
    }
  }

  // ============================================================
  // ELIMINAR
  // ============================================================

  eliminar(id: number): void {
    Swal.fire({
      title: '¿Eliminar lote?',
      text: 'Esta acción no se puede deshacer',
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
        this.loteService.eliminar(id)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.lotes = this.lotes.filter(l => l.id !== id);
              this.aplicarFiltros();
              this.calcularAlertas();
              Swal.fire('Eliminado', 'Lote eliminado correctamente', 'success');
              this.cdr.detectChanges();
            },
            error: (err) => {
              Swal.fire('Error', err.error?.mensaje || 'No se pudo eliminar el lote', 'error');
            }
          });
      }
    });
  }

  // ============================================================
  // MARCAR DETERIORADO
  // ============================================================

  marcarDeteriorado(id: number): void {
    Swal.fire({
      title: '¿Marcar como deteriorado?',
      text: 'Este lote pasará a estado "deteriorado" y no podrá ser vendido.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#f59e0b',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, marcar',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (result.isConfirmed) {
        this.loteService.marcarDeteriorado(id)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              const lote = this.lotes.find(l => l.id === id);
              if (lote) lote.estado = 'deteriorado';
              this.aplicarFiltros();
              this.calcularAlertas();
              Swal.fire('Actualizado', 'Lote marcado como deteriorado', 'success');
              this.cdr.detectChanges();
            },
            error: (err) => {
              Swal.fire('Error', err.error?.mensaje || 'Error al marcar deteriorado', 'error');
            }
          });
      }
    });
  }

  // ============================================================
  // ALERTAS
  // ============================================================

  calcularAlertas(): void {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const limite = new Date(hoy);
    limite.setDate(limite.getDate() + 30);
    this.stockBajo = this.lotes.filter(l => l.estado === 'activo' && l.cantidad <= 10);
    this.proximoVencer = this.lotes.filter(l => {
      if (l.estado !== 'activo') return false;
      const venc = new Date(l.fechaVencimiento);
      venc.setHours(0, 0, 0, 0);
      return venc >= hoy && venc <= limite;
    });
  }

  diasParaVencer(fechaVencimiento: string): number {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const venc = new Date(fechaVencimiento);
    venc.setHours(0, 0, 0, 0);
    return Math.ceil((venc.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24));
  }

  getEstadoBadge(estado: string): string {
    const map: Record<string, string> = {
      'activo': 'bg-success text-white',
      'deteriorado': 'bg-warning text-dark',
      'vencido': 'bg-danger text-white',
      'agotado': 'bg-secondary text-white'
    };
    return map[estado] || 'bg-secondary text-white';
  }

  // ============================================================
  // IMAGEN
  // ============================================================

  public obtenerUrlCompleta(ruta: string): string {
    if (!ruta) return '';
    if (ruta.startsWith('http://') || ruta.startsWith('https://')) return ruta;
    const hostBase = environment.apiUrl.replace(/\/api\/?$/, '');
    const rutaLimpia = ruta.startsWith('/') ? ruta : `/${ruta}`;
    return `${hostBase}${rutaLimpia}`;
  }

  obtenerImagenProducto(lote: Lote): string {
    if (lote.productoImagen) {
      return lote.productoImagen;
    }
    return 'assets/placeholder-producto.svg';
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/placeholder-producto.svg';
  }

  // ============================================================
  // MODALES DE SELECCIÓN (PRODUCTO Y PROVEEDOR)
  // ============================================================

  abrirModalProductos(): void {
    this.modalProductosAbierto = true;
    this.filtroProductoModal = '';
    this.cargarProductosModal();
    this.cdr.detectChanges();
  }

  cerrarModalProductos(): void {
    this.modalProductosAbierto = false;
    this.cdr.detectChanges();
  }

  abrirModalProveedores(): void {
    this.modalProveedoresAbierto = true;
    this.filtroProveedorModal = '';
    this.cargarProveedoresModal();
    this.cdr.detectChanges();
  }

  cerrarModalProveedores(): void {
    this.modalProveedoresAbierto = false;
    this.cdr.detectChanges();
  }

  cargarProductosModal(): void {
    if (this.cargandoProductosModal) return;
    this.cargandoProductosModal = true;
    this.productoService.listar().pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.productosModal = data;
        this.cargandoProductosModal = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargandoProductosModal = false;
      }
    });
  }

  cargarProveedoresModal(): void {
    if (this.cargandoProveedoresModal) return;
    this.cargandoProveedoresModal = true;
    this.proveedorService.listar().pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.proveedoresModal = data;
        this.cargandoProveedoresModal = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargandoProveedoresModal = false;
      }
    });
  }

  buscarProductosModal(): void {
    if (!this.filtroProductoModal.trim()) {
      this.cargarProductosModal();
      return;
    }
    this.productoService.buscarPorNombre(this.filtroProductoModal)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.productosModal = data;
          this.cdr.detectChanges();
        },
        error: (err) => console.error(err)
      });
  }

  buscarProveedoresModal(): void {
    if (!this.filtroProveedorModal.trim()) {
      this.cargarProveedoresModal();
      return;
    }
    this.proveedorService.buscarPorRazonSocial(this.filtroProveedorModal)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.proveedoresModal = data;
          this.cdr.detectChanges();
        },
        error: (err) => console.error(err)
      });
  }

  seleccionarProducto(producto: Producto): void {
    this.formCrear.patchValue({ productoId: producto.id });
    this.cerrarModalProductos();
    this.cdr.detectChanges();
  }

  seleccionarProveedor(proveedor: Proveedor): void {
    this.formCrear.patchValue({ proveedorId: proveedor.id });
    this.cerrarModalProveedores();
    this.cdr.detectChanges();
  }

  getProductoNombre(id: number | undefined): string {
    if (id == null) return 'Seleccionar producto...';
    const p = this.productos.find(p => p.id === id);
    return p ? p.nombre : 'Producto no encontrado';
  }

  getProveedorNombre(id: number | undefined): string {
    if (id == null) return 'Seleccionar proveedor...';
    const p = this.proveedores.find(p => p.id === id);
    return p ? p.razonSocial : 'Proveedor no encontrado';
  }

  // ============================================================
  // OFFCANVAS CREAR (con auto-generación de lote y fecha)
  // ============================================================
  abrirOffcanvasCrear(): void {
    const hoy = new Date();
    const anio = hoy.getFullYear();

    // Generar número de lote automático
    const lotesAnio = this.lotes.filter(l => l.lote.startsWith(`LOT${anio}`));
    let numero = 1;
    if (lotesAnio.length > 0) {
      const ultimoLote = lotesAnio.sort((a, b) => b.lote.localeCompare(a.lote))[0];
      const partes = ultimoLote.lote.split('-');
      if (partes.length === 2) {
        numero = parseInt(partes[1], 10) + 1;
      }
    }
    const loteGenerado = `LOT${anio}-${String(numero).padStart(3, '0')}`;

    // Asegurar que usuarioId sea válido
    const usuarioValido = this.usuarioId > 0 ? this.usuarioId : null;

    // Resetea el formulario sin incluir fechaIngreso
    this.formCrear.reset({
      productoId: null,
      proveedorId: null,
      lote: loteGenerado,
      fechaVencimiento: '',
      cantidad: 1,
      precioCompra: 0,
      precioVenta: 0,
      usuarioId: usuarioValido
    });

    // Actualizar fechas por si cambió el día
    this.actualizarFechas();

    this.offcanvasCrearAbierto = true;
    this.cdr.detectChanges();
  }

  cerrarOffcanvasCrear(): void {
    this.offcanvasCrearAbierto = false;
    this.guardando = false;
  }

  guardarLote(): void {
    // Verificar si el formulario es inválido
    if (this.formCrear.invalid) {
      this.formCrear.markAllAsTouched();
      
      // Mostrar mensaje más específico
      let mensaje = 'Completa todos los campos obligatorios.';
      if (this.formCrear.get('usuarioId')?.invalid) {
        mensaje = 'Usuario no autenticado. Inicia sesión nuevamente.';
      }
      Swal.fire('Formulario incompleto', mensaje, 'warning');
      return;
    }

    this.guardando = true;
    
    // Construir datos asegurando que fechaIngreso sea la actual
    const data = {
      ...this.formCrear.value,
      fechaIngreso: this.fechaIngresoISO  // Fecha actual en formato YYYY-MM-DD
    };

    console.log('Datos a enviar:', data); // Para depurar

    this.loteService.crear(data)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.guardando = false;
          this.cerrarOffcanvasCrear();
          this.cargarLotes();
          Swal.fire('Éxito', 'Lote creado exitosamente', 'success');
        },
        error: (err) => {
          this.guardando = false;
          console.error('Error al crear lote:', err);
          
          // Mostrar error más detallado
          let mensajeError = err.error?.mensaje || 'Error al crear lote';
          if (err.error?.errors) {
            mensajeError = Object.values(err.error.errors).join(' ');
          }
          Swal.fire('Error', mensajeError, 'error');
        }
      });
  }

  // ============================================================
  // MODAL EDITAR
  // ============================================================

  abrirModalEditar(id: number): void {
    this.loteIdEditar = id;
    this.modalEditarAbierto = true;
    this.cargando = true;
    this.loteService.obtener(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: Lote) => {
          this.formEditar.patchValue({
            productoId: data.productoId,
            proveedorId: data.proveedorId,
            lote: data.lote,
            fechaIngreso: data.fechaIngreso,
            fechaVencimiento: data.fechaVencimiento,
            cantidad: data.cantidad,
            precioCompra: data.precioCompra,
            precioVenta: data.precioVenta
          });
          this.cargando = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.cargando = false;
          this.cerrarModalEditar();
          Swal.fire('Error', 'No se pudo cargar el lote', 'error');
        }
      });
  }

  cerrarModalEditar(): void {
    this.modalEditarAbierto = false;
    this.guardandoEditar = false;
    this.formEditar.reset();
  }

  actualizarLote(): void {
    if (this.formEditar.invalid) {
      this.formEditar.markAllAsTouched();
      Swal.fire('Formulario incompleto', 'Completa todos los campos correctamente.', 'warning');
      return;
    }
    this.guardandoEditar = true;
    const data = this.formEditar.value;

    this.loteService.actualizar(this.loteIdEditar, data)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.guardandoEditar = false;
          this.cerrarModalEditar();
          this.cargarLotes();
          Swal.fire('Éxito', 'Lote actualizado correctamente', 'success');
        },
        error: (err) => {
          this.guardandoEditar = false;
          Swal.fire('Error', err.error?.mensaje || 'Error al actualizar lote', 'error');
        }
      });
  }

  // ============================================================
  // MODAL AJUSTAR STOCK
  // ============================================================

  abrirModalAjustar(id: number): void {
    this.loteIdAjustar = id;
    this.modalAjustarAbierto = true;
    this.cargando = true;
    this.loteService.obtener(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: Lote) => {
          this.loteAjustar = data;
          this.formAjustar.patchValue({
            tipoMovimiento: 'ajuste',
            cantidad: '',
            observacion: ''
          });
          this.cargando = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.cargando = false;
          this.cerrarModalAjustar();
          Swal.fire('Error', 'No se pudo cargar el lote', 'error');
        }
      });
  }

  cerrarModalAjustar(): void {
    this.modalAjustarAbierto = false;
    this.guardandoAjustar = false;
    this.loteAjustar = null;
    this.formAjustar.reset({ tipoMovimiento: 'ajuste' });
  }

  guardarAjusteStock(): void {
    if (this.formAjustar.invalid) {
      Swal.fire('Formulario incompleto', 'Ingresa una cantidad válida.', 'warning');
      return;
    }
    const request = {
      cantidad: this.formAjustar.value.cantidad,
      tipoMovimiento: this.formAjustar.value.tipoMovimiento,
      observacion: this.formAjustar.value.observacion || '',
      usuarioId: this.usuarioId
    };
    this.guardandoAjustar = true;
    this.loteService.ajustarStock(this.loteIdAjustar, request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.guardandoAjustar = false;
          this.cerrarModalAjustar();
          this.cargarLotes();
          Swal.fire('Éxito', response.mensaje || 'Stock ajustado correctamente', 'success');
        },
        error: (err) => {
          this.guardandoAjustar = false;
          Swal.fire('Error', err.error?.mensaje || 'Error al ajustar stock', 'error');
        }
      });
  }

  // ============================================================
  // MODAL DETALLE
  // ============================================================

  abrirModalDetalle(id: number): void {
    this.modalDetalleAbierto = true;
    this.cargando = true;
    this.loteService.obtener(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: Lote) => {
          this.loteDetalle = data;
          this.cargando = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.cargando = false;
          this.cerrarDetalle();
          Swal.fire('Error', 'No se pudo cargar el detalle del lote', 'error');
        }
      });
  }

  cerrarDetalle(): void {
    this.modalDetalleAbierto = false;
    this.loteDetalle = null;
    this.cdr.detectChanges();
  }

  // ============================================================
  // UTILIDADES
  // ============================================================

  trackByLoteId(index: number, lote: Lote): number {
    return lote.id!;
  }
}