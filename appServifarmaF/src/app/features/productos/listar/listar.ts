import { Component, OnInit, ChangeDetectorRef, HostListener, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductoService } from '../../../core/services/producto';
import { CategoriaService } from '../../../core/services/categoria';
import { FabricanteService } from '../../../core/services/fabricante';
import { Producto } from '../../../core/models/producto.model';
import { Categoria } from '../../../core/models/categoria.model';
import { Fabricante } from '../../../core/models/fabricante.model';
import { AuthService } from '../../../core/auth';
import Swal from 'sweetalert2';
import { Subject, takeUntil } from 'rxjs';

/** Solo dígitos, entre 6 y 14 caracteres (cubre UPC-A, EAN-13, códigos internos, etc.) */
const PATRON_CODIGO_BARRAS = /^[0-9]{6,14}$/;

@Component({
  selector: 'app-listar-productos',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit, OnDestroy {
  // ======== PROPIEDADES ========
  productos: Producto[] = [];
  productosFiltrados: Producto[] = [];
  productosMostrados: Producto[] = [];
  categorias: Categoria[] = [];
  fabricantes: Fabricante[] = [];
  filtroTexto: string = '';
  filtroCategoria: string = '';
  filtroEstado: string = '';
  cargando: boolean = false;
  isAdmin: boolean = false;

  /** id del producto que se está eliminando (bloquea su botón, evita doble-click) */
  eliminandoId: number | null = null;

  // ======== SCROLL INFINITO ========
  itemsPorPagina: number = 8;
  paginaActual: number = 1;
  cargandoMas: boolean = false;
  todosCargados: boolean = false;

  // ======== OFFCANVAS CREAR ========
  offcanvasCrearAbierto: boolean = false;
  enviandoCrear: boolean = false;
  formCrear: FormGroup;

  // ======== MODAL EDITAR ========
  modalEditarAbierto: boolean = false;
  enviandoEditar: boolean = false;
  formEditar: FormGroup;
  productoIdEditar!: number;

  // ======== MODAL DETALLE ========
  modalDetalleAbierto: boolean = false;
  productoDetalle: Producto | null = null;

  // ======== MODAL IMPORTAR ========
  modalImportarAbierto: boolean = false;
  archivoImportar: File | null = null;
  importando: boolean = false;
  resultadoImportacion: any = null;

  // ======== IMAGEN LOCAL ========
  imagenArchivo: File | null = null;
  imagenPrevisualizacion: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private productoService: ProductoService,
    private categoriaService: CategoriaService,
    private fabricanteService: FabricanteService,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    public auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    // ======== FORMULARIO DE CREACIÓN ========
    this.formCrear = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      codigoBarras: ['', [Validators.maxLength(50), Validators.pattern(PATRON_CODIGO_BARRAS)]],
      principioActivo: ['', [Validators.maxLength(150)]],
      imagen: ['', [Validators.maxLength(255)]],
      esGenerico: [false],
      precioCompraActual: [0, [Validators.required, Validators.min(0.01)]],
      precioVentaActual: [0, [Validators.required, Validators.min(0.01)]],
      stockMinimo: [5, [Validators.required, Validators.min(1)]],
      categoriaId: [null],
      fabricanteId: [null],
      productoGenericoId: [null]
    });

    // ======== FORMULARIO DE EDICIÓN ========
    this.formEditar = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      codigoBarras: ['', [Validators.maxLength(50), Validators.pattern(PATRON_CODIGO_BARRAS)]],
      principioActivo: ['', [Validators.maxLength(150)]],
      imagen: ['', [Validators.maxLength(255)]],
      esGenerico: [false],
      precioCompraActual: [0, [Validators.required, Validators.min(0.01)]],
      precioVentaActual: [0, [Validators.required, Validators.min(0.01)]],
      stockMinimo: [5, [Validators.required, Validators.min(1)]],
      categoriaId: [null],
      fabricanteId: [null],
      productoGenericoId: [null]
    });
  }

  ngOnInit(): void {
    this.isAdmin = this.auth.isAdmin();
    this.cargarCategorias();
    this.cargarFabricantes();
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      if (params['stock'] === 'bajo') {
        this.filtroEstado = 'sin-stock';
      }
      this.cargarProductos();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ======== MÉTODOS DE CARGA ========
  cargarCategorias(): void {
    this.categoriaService.listar().pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => this.categorias = data,
      error: (err) => console.error('Error al cargar categorías:', err)
    });
  }

  cargarFabricantes(): void {
    this.fabricanteService.listar().pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => this.fabricantes = data,
      error: (err) => console.error('Error al cargar fabricantes:', err)
    });
  }

  cargarProductos(): void {
    if (this.cargando) return;
    this.cargando = true;
    this.productoService.listar().pipe(takeUntil(this.destroy$)).subscribe({
      next: (data: Producto[]) => {
        this.productos = data;
        this.aplicarFiltros();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error al cargar productos:', err);
        this.cargando = false;
        Swal.fire({ title: 'Error', text: 'No se pudieron cargar los productos', icon: 'error', customClass: { popup: 'swal-farmaceutico' } });
        this.cdr.detectChanges();
      }
    });
  }

  // ======== OBTENER NOMBRES ========
  obtenerNombreCategoria(categoriaId: number | null | undefined): string {
    if (!categoriaId) return 'Sin categoría';
    const categoria = this.categorias.find(c => c.id === categoriaId);
    return categoria ? categoria.nombre : 'Sin categoría';
  }

  obtenerNombreFabricante(fabricanteId: number | null | undefined): string {
    if (!fabricanteId) return 'Sin fabricante';
    const fabricante = this.fabricantes.find(f => f.id === fabricanteId);
    return fabricante ? fabricante.nombre : 'Sin fabricante';
  }

  // ======== MANEJO DE ERROR DE IMAGEN ========
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
    const contenedor = img.parentElement;
    const fallback = contenedor?.querySelector('.placeholder-icon-fallback') as HTMLElement | null;
    if (fallback) { fallback.style.display = 'flex'; }
  }

  // ======== IMAGEN LOCAL ========
  onImagenSeleccionada(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const archivo = input.files[0];

      if (!archivo.type.startsWith('image/')) {
        Swal.fire({ title: 'Archivo inválido', text: 'Selecciona un archivo de imagen (JPG, PNG, GIF, WEBP).', icon: 'warning', customClass: { popup: 'swal-farmaceutico' } });
        return;
      }
      if (archivo.size > 5 * 1024 * 1024) {
        Swal.fire({ title: 'Imagen muy pesada', text: 'El tamaño máximo permitido es 5 MB.', icon: 'warning', customClass: { popup: 'swal-farmaceutico' } });
        return;
      }

      this.imagenArchivo = archivo;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagenPrevisualizacion = e.target?.result as string;
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(this.imagenArchivo);
    }
  }

  limpiarImagenSeleccionada(): void {
    this.imagenArchivo = null;
    this.imagenPrevisualizacion = null;
  }

  // ======== FILTROS ========
  aplicarFiltros(): void {
    this.productosFiltrados = this.productos.filter(p => {
      if (this.filtroTexto.trim()) {
        const texto = this.filtroTexto.toLowerCase().trim();
        const nombreMatch = p.nombre.toLowerCase().includes(texto);
        const codigoMatch = p.codigoBarras?.toLowerCase().includes(texto) || false;
        if (!nombreMatch && !codigoMatch) return false;
      }
      if (this.filtroCategoria) {
        if (p.categoriaId !== Number(this.filtroCategoria)) return false;
      }
      if (this.filtroEstado === 'con-stock' && (p.stockActual || 0) <= 0) return false;
      if (this.filtroEstado === 'sin-stock' && (p.stockActual || 0) > 0) return false;
      return true;
    });
    this.paginaActual = 1;
    this.todosCargados = false;
    this.cargarMasProductos();
  }

  buscar(): void {
    if (this.filtroTexto.trim()) {
      this.productoService.buscarPorNombreOCodigo(this.filtroTexto).pipe(takeUntil(this.destroy$)).subscribe({
        next: (data: Producto[]) => {
          this.productos = data;
          this.aplicarFiltros();
          this.cdr.detectChanges();
        },
        error: (err: any) => console.error('Error al buscar:', err)
      });
    } else {
      this.cargarProductos();
    }
  }

  limpiarFiltros(): void {
    this.filtroTexto = '';
    this.filtroCategoria = '';
    this.filtroEstado = '';
    this.router.navigate(['/productos']);
    this.cargarProductos();
  }

  // ======== SCROLL INFINITO ========
  cargarMasProductos(): void {
    if (this.todosCargados || this.cargandoMas) return;
    const fin = this.paginaActual * this.itemsPorPagina;
    this.productosMostrados = this.productosFiltrados.slice(0, fin);
    if (fin >= this.productosFiltrados.length) {
      this.todosCargados = true;
    }
    this.cdr.detectChanges();
  }

  @HostListener('window:scroll')
  onScroll(): void {
    if (this.cargando || this.todosCargados || this.cargandoMas || this.productosFiltrados.length === 0) return;

    const scrollTop = window.scrollY || document.documentElement.scrollTop;
    const windowHeight = window.innerHeight;
    const documentHeight = document.documentElement.scrollHeight;

    if (scrollTop + windowHeight >= documentHeight - 150) {
      this.cargandoMas = true;
      setTimeout(() => {
        this.paginaActual++;
        this.cargarMasProductos();
        this.cargandoMas = false;
        this.cdr.detectChanges();
      }, 300);
    }
  }

  // ======== ELIMINAR (con actualización instantánea) ========
  eliminar(id: number): void {
    if (!id || this.eliminandoId !== null) return;

    const producto = this.productos.find(p => p.id === id);

    Swal.fire({
      title: '¿Eliminar producto?',
      html: `
        <p style="color:#475569;margin-bottom:8px;">Esta acción no se puede deshacer.</p>
        <div style="background:#f8fafc;border-radius:8px;padding:12px;border-left:4px solid #dc2626;text-align:left;">
          <strong>${producto?.nombre || 'Producto #' + id}</strong>
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
        this.productoService.eliminar(id).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.productos = this.productos.filter(p => p.id !== id);
            this.aplicarFiltros();
            this.eliminandoId = null;
            this.cargarProductos(); // Recarga para consistencia
            Swal.fire({
              title: 'Eliminado',
              text: 'Producto eliminado correctamente',
              icon: 'success',
              timer: 2200,
              showConfirmButton: false,
              customClass: { popup: 'swal-farmaceutico' }
            });
          },
          error: (err: any) => {
            console.error('Error al eliminar:', err);
            this.eliminandoId = null;
            const mensaje = err.error?.mensaje || err.error?.message || 'No se pudo eliminar el producto';
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

  // ======== OFFCANVAS CREAR ========
  abrirOffcanvasCrear(): void {
    this.formCrear.reset({
      nombre: '',
      codigoBarras: '',
      principioActivo: '',
      imagen: '',
      esGenerico: false,
      precioCompraActual: 0,
      precioVentaActual: 0,
      stockMinimo: 5,
      categoriaId: null,
      fabricanteId: null,
      productoGenericoId: null
    });
    this.limpiarImagenSeleccionada();
    this.offcanvasCrearAbierto = true;
  }

  cerrarOffcanvasCrear(): void {
    if (this.enviandoCrear) return;
    this.offcanvasCrearAbierto = false;
    this.limpiarImagenSeleccionada();
  }

  guardarProducto(): void {
    if (this.enviandoCrear) return;

    if (this.formCrear.invalid) {
      this.formCrear.markAllAsTouched();
      Swal.fire({
        title: 'Formulario incompleto',
        text: 'Completa todos los campos obligatorios correctamente.',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    const data = { ...this.formCrear.value };
    const imagenLocal = this.imagenArchivo;

    this.enviandoCrear = true;
    this.productoService.crear(data).pipe(takeUntil(this.destroy$)).subscribe({
      next: (productoCreado) => {
        if (imagenLocal) {
          this.productoService.subirImagen(productoCreado.id!, imagenLocal).subscribe({
            next: () => {
              this.enviandoCrear = false;
              this.cerrarOffcanvasCrear();
              this.cargarProductos();
              Swal.fire({
                title: 'Éxito',
                text: 'Producto creado con imagen correctamente',
                icon: 'success',
                customClass: { popup: 'swal-farmaceutico' }
              });
            },
            error: () => {
              this.enviandoCrear = false;
              this.cerrarOffcanvasCrear();
              this.cargarProductos();
              Swal.fire({
                title: 'Advertencia',
                text: 'Producto creado pero no se pudo subir la imagen',
                icon: 'warning',
                customClass: { popup: 'swal-farmaceutico' }
              });
            }
          });
        } else {
          this.enviandoCrear = false;
          this.cerrarOffcanvasCrear();
          this.cargarProductos();
          Swal.fire({
            title: 'Éxito',
            text: 'Producto creado exitosamente',
            icon: 'success',
            customClass: { popup: 'swal-farmaceutico' }
          });
        }
      },
      error: (err) => {
        this.enviandoCrear = false;
        const mensaje = err.error?.mensaje || err.error?.message || 'Error al crear producto';
        Swal.fire({
          title: 'Error',
          text: mensaje,
          icon: 'error',
          customClass: { popup: 'swal-farmaceutico' }
        });
      }
    });
  }

  // ======== MODAL EDITAR ========
  abrirModalEditar(id: number): void {
    this.productoIdEditar = id;
    this.modalEditarAbierto = true;
    this.cargando = true;
    this.productoService.obtener(id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data: Producto) => {
        this.formEditar.patchValue({
          nombre: data.nombre,
          codigoBarras: data.codigoBarras,
          principioActivo: data.principioActivo,
          imagen: data.imagen,
          esGenerico: data.esGenerico,
          // ✅ CORREGIDO: precioCompraActual no viene del backend, se deja en 0
          precioCompraActual: 0,
          precioVentaActual: data.precioVentaActual,
          stockMinimo: data.stockMinimo,
          categoriaId: data.categoriaId || null,
          fabricanteId: data.fabricanteId || null,
          productoGenericoId: data.productoGenericoId || null
        });
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.cargando = false;
        this.cerrarModalEditar();
        Swal.fire({
          title: 'Error',
          text: 'No se pudo cargar el producto',
          icon: 'error',
          customClass: { popup: 'swal-farmaceutico' }
        });
      }
    });
  }

  cerrarModalEditar(): void {
    if (this.enviandoEditar) return;
    this.modalEditarAbierto = false;
    this.formEditar.reset();
  }

  actualizarProducto(): void {
    if (this.enviandoEditar) return;

    if (this.formEditar.invalid) {
      this.formEditar.markAllAsTouched();
      Swal.fire({
        title: 'Formulario incompleto',
        text: 'Completa todos los campos obligatorios correctamente.',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }
    this.enviandoEditar = true;
    this.productoService.actualizar(this.productoIdEditar, this.formEditar.value).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.enviandoEditar = false;
        this.cerrarModalEditar();
        this.cargarProductos();
        Swal.fire({
          title: 'Éxito',
          text: 'Producto actualizado exitosamente',
          icon: 'success',
          customClass: { popup: 'swal-farmaceutico' }
        });
      },
      error: (err) => {
        this.enviandoEditar = false;
        const mensaje = err.error?.mensaje || err.error?.message || 'Error al actualizar producto';
        Swal.fire({
          title: 'Error',
          text: mensaje,
          icon: 'error',
          customClass: { popup: 'swal-farmaceutico' }
        });
      }
    });
  }

  // ======== MODAL DETALLE ========
  abrirModalDetalle(producto: Producto): void {
    this.productoDetalle = producto;
    this.modalDetalleAbierto = true;
  }

  cerrarModalDetalle(): void {
    this.modalDetalleAbierto = false;
    this.productoDetalle = null;
  }

  // ======== MODAL IMPORTAR ========
  abrirModalImportar(): void {
    this.modalImportarAbierto = true;
    this.archivoImportar = null;
    this.resultadoImportacion = null;
    this.importando = false;
  }

  cerrarModalImportar(): void {
    if (this.importando) return;
    this.modalImportarAbierto = false;
    this.archivoImportar = null;
    this.resultadoImportacion = null;
  }

  onArchivoSeleccionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.archivoImportar = input.files[0];
    }
  }

  descargarPlantilla(): void {
    this.productoService.descargarPlantilla().pipe(takeUntil(this.destroy$)).subscribe({
      next: (blob) => {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'plantilla_productos.xlsx';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(link.href);
        Swal.fire({
          title: 'Éxito',
          text: 'Plantilla descargada correctamente',
          icon: 'success',
          timer: 2000,
          showConfirmButton: false,
          customClass: { popup: 'swal-farmaceutico' }
        });
      },
      error: () => Swal.fire({
        title: 'Error',
        text: 'No se pudo descargar la plantilla',
        icon: 'error',
        customClass: { popup: 'swal-farmaceutico' }
      })
    });
  }

  importarProductos(): void {
    if (this.importando) return;

    if (!this.archivoImportar) {
      Swal.fire({
        title: 'Advertencia',
        text: 'Selecciona un archivo primero',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    this.importando = true;
    this.productoService.importarProductos(this.archivoImportar).pipe(takeUntil(this.destroy$)).subscribe({
      next: (res) => {
        this.resultadoImportacion = res;
        this.importando = false;
        if (res.errores === 0) {
          Swal.fire({
            title: 'Éxito',
            text: `${res.importados} productos importados correctamente`,
            icon: 'success',
            customClass: { popup: 'swal-farmaceutico' }
          });
          this.cerrarModalImportar();
          this.cargarProductos();
        } else {
          Swal.fire({
            title: 'Importación parcial',
            text: `${res.importados} importados, ${res.errores} errores. Revisa los detalles.`,
            icon: 'warning',
            customClass: { popup: 'swal-farmaceutico' }
          });
        }
      },
      error: (err) => {
        this.importando = false;
        const mensaje = err.error?.mensaje || err.error?.message || 'No se pudo importar el archivo';
        Swal.fire({
          title: 'Error',
          text: mensaje,
          icon: 'error',
          customClass: { popup: 'swal-farmaceutico' }
        });
      }
    });
  }

  // ======== TRACK BY ========
  trackByProductoId(index: number, producto: Producto): number {
    return producto.id!;
  }
}