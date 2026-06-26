import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductoService } from '../../../core/services/producto';
import { CategoriaService } from '../../../core/services/categoria';
import { FabricanteService } from '../../../core/services/fabricante';
import { Producto } from '../../../core/models/producto.model';
import { Categoria } from '../../../core/models/categoria.model';
import { Fabricante } from '../../../core/models/fabricante.model';

@Component({
  selector: 'app-listar-productos',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  // ======== PROPIEDADES EXISTENTES ========
  productos: Producto[] = [];
  productosFiltrados: Producto[] = [];
  categorias: Categoria[] = [];
  fabricantes: Fabricante[] = [];
  filtroTexto: string = '';
  filtroCategoria: string = '';
  filtroEstado: string = '';
  cargando: boolean = false;

  // ======== OFFCANVAS CREAR ========
  offcanvasCrearAbierto: boolean = false;
  enviandoCrear: boolean = false;
  formCrear: FormGroup;

  // ======== MODAL EDITAR ========
  modalEditarAbierto: boolean = false;
  enviandoEditar: boolean = false;
  formEditar: FormGroup;
  productoIdEditar!: number;

  constructor(
    private productoService: ProductoService,
    private categoriaService: CategoriaService,
    private fabricanteService: FabricanteService,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router
  ) {
    // Formulario de creación
    this.formCrear = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      codigoBarras: ['', [Validators.maxLength(50)]],
      principioActivo: ['', [Validators.maxLength(150)]],
      imagen: ['', [Validators.maxLength(255)]],
      esGenerico: [false],
      precioVentaActual: [0, [Validators.required, Validators.min(0.01)]],
      stockMinimo: [5, [Validators.required, Validators.min(1)]],
      categoriaId: [null],
      fabricanteId: [null],
      productoGenericoId: [null]
    });

    // Formulario de edición (idéntico)
    this.formEditar = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      codigoBarras: ['', [Validators.maxLength(50)]],
      principioActivo: ['', [Validators.maxLength(150)]],
      imagen: ['', [Validators.maxLength(255)]],
      esGenerico: [false],
      precioVentaActual: [0, [Validators.required, Validators.min(0.01)]],
      stockMinimo: [5, [Validators.required, Validators.min(1)]],
      categoriaId: [null],
      fabricanteId: [null],
      productoGenericoId: [null]
    });
  }

  ngOnInit(): void {
    this.cargarCategorias();
    this.cargarFabricantes();
    this.route.queryParams.subscribe(params => {
      if (params['stock'] === 'bajo') {
        this.filtroEstado = 'sin-stock';
      }
      this.cargarProductos();
    });
  }

  // ======== MÉTODOS DE CARGA ========
  cargarCategorias(): void {
    this.categoriaService.listar().subscribe({
      next: (data) => this.categorias = data,
      error: (err) => console.error('Error al cargar categorías:', err)
    });
  }

  cargarFabricantes(): void {
    this.fabricanteService.listar().subscribe({
      next: (data) => this.fabricantes = data,
      error: (err) => console.error('Error al cargar fabricantes:', err)
    });
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
    // Aquí debes implementar la lógica real para obtener el stock
    // Ejemplo: llamar a un servicio que devuelva el stock actual
    return 0;
  }

  // ======== FILTROS ========
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

  limpiarFiltros(): void {
    this.filtroTexto = '';
    this.filtroCategoria = '';
    this.filtroEstado = '';
    this.router.navigate(['/productos']);
    this.cargarProductos();
  }

  // ======== OBTENER NOMBRE DE CATEGORÍA ========
  obtenerNombreCategoria(categoriaId: number | null | undefined): string {
    if (!categoriaId) return 'Sin categoría';
    const categoria = this.categorias.find(c => c.id === categoriaId);
    return categoria ? categoria.nombre : 'Sin categoría';
  }

  // ======== ELIMINAR ========
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

  // ======== OFFCANVAS CREAR ========
  abrirOffcanvasCrear(): void {
    this.formCrear.reset({
      nombre: '',
      codigoBarras: '',
      principioActivo: '',
      imagen: '',
      esGenerico: false,
      precioVentaActual: 0,
      stockMinimo: 5,
      categoriaId: null,
      fabricanteId: null,
      productoGenericoId: null
    });
    this.offcanvasCrearAbierto = true;
  }

  cerrarOffcanvasCrear(): void {
    this.offcanvasCrearAbierto = false;
    this.enviandoCrear = false;
  }

  guardarProducto(): void {
    if (this.formCrear.invalid) {
      alert('Completa todos los campos obligatorios correctamente.');
      return;
    }
    this.enviandoCrear = true;
    this.productoService.crear(this.formCrear.value).subscribe({
      next: () => {
        this.enviandoCrear = false;
        this.cerrarOffcanvasCrear();
        this.cargarProductos();
        alert('Producto creado exitosamente');
      },
      error: (err) => {
        this.enviandoCrear = false;
        alert(err.error?.mensaje || 'Error al crear producto');
      }
    });
  }

  // ======== MODAL EDITAR ========
  abrirModalEditar(id: number): void {
    this.productoIdEditar = id;
    this.modalEditarAbierto = true;
    this.cargando = true;
    this.productoService.obtener(id).subscribe({
      next: (data: Producto) => {
        this.formEditar.patchValue({
          nombre: data.nombre,
          codigoBarras: data.codigoBarras,
          principioActivo: data.principioActivo,
          imagen: data.imagen,
          esGenerico: data.esGenerico,
          precioVentaActual: data.precioVentaActual,
          stockMinimo: data.stockMinimo,
          categoriaId: data.categoriaId || null,
          fabricanteId: data.fabricanteId || null,
          productoGenericoId: data.productoGenericoId || null
        });
        this.cargando = false;
      },
      error: (err) => {
        console.error(err);
        this.cargando = false;
        this.cerrarModalEditar();
      }
    });
  }

  cerrarModalEditar(): void {
    this.modalEditarAbierto = false;
    this.enviandoEditar = false;
    this.formEditar.reset();
  }

  actualizarProducto(): void {
    if (this.formEditar.invalid) {
      alert('Por favor completa todos los campos correctamente.');
      return;
    }
    this.enviandoEditar = true;
    this.productoService.actualizar(this.productoIdEditar, this.formEditar.value).subscribe({
      next: () => {
        this.enviandoEditar = false;
        this.cerrarModalEditar();
        this.cargarProductos();
        alert('Producto actualizado exitosamente');
      },
      error: (err) => {
        this.enviandoEditar = false;
        alert(err.error?.mensaje || 'Error al actualizar producto');
      }
    });
  }
}