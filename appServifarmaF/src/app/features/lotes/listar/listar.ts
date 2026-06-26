import { Component, OnInit } from '@angular/core';
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

@Component({
  selector: 'app-listar-lotes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  // ======== PROPIEDADES EXISTENTES ========
  lotes: Lote[] = [];
  lotesFiltrados: Lote[] = [];
  filtroProducto: string = '';
  filtroEstado: string = '';
  filtroVencimiento: string = '';
  cargando: boolean = false;
  stockBajo: Lote[] = [];
  proximoVencer: Lote[] = [];

  // ======== OFFCANVAS CREAR ========
  offcanvasCrearAbierto: boolean = false;
  guardando: boolean = false;
  nuevoLote: any = {
    productoId: null,
    lote: '',
    fechaVencimiento: '',
    cantidad: 1,
    precioVenta: 0,
    estado: 'activo'
  };
  productos: Producto[] = [];

  // ======== MODAL EDITAR ========
  modalEditarAbierto: boolean = false;
  guardandoEditar: boolean = false;
  formEditar: FormGroup;
  loteIdEditar!: number;
  proveedores: Proveedor[] = [];

  // ======== MODAL AJUSTAR STOCK ========
  modalAjustarAbierto: boolean = false;
  guardandoAjustar: boolean = false;
  formAjustar: FormGroup;
  loteAjustar: Lote | null = null;
  loteIdAjustar!: number;
  usuarioId: number = 0;

  constructor(
    private loteService: LoteService,
    private productoService: ProductoService,
    private proveedorService: ProveedorService,
    private authService: AuthService,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router
  ) {
    // Formulario editar
    this.formEditar = this.fb.group({
      productoId: ['', Validators.required],
      proveedorId: ['', Validators.required],
      lote: ['', [Validators.required, Validators.maxLength(50)]],
      fechaIngreso: ['', Validators.required],
      fechaVencimiento: ['', Validators.required],
      cantidad: ['', [Validators.required, Validators.min(1)]],
      precioCompra: ['', [Validators.required, Validators.min(0.01)]],
      precioVenta: ['', [Validators.required, Validators.min(0.01)]]
    });

    // Formulario ajustar stock
    this.formAjustar = this.fb.group({
      tipoMovimiento: ['ajuste', Validators.required],
      cantidad: ['', [Validators.required, Validators.min(1)]],
      observacion: ['']
    });
  }

  ngOnInit(): void {
    this.route.url.subscribe(url => {
      const path = url.map(seg => seg.path).join('/');
      this.filtroVencimiento = path.includes('proximos-a-vencer') ? 'proximo' : '';
      this.cargarLotes();
    });
    this.cargarProductos();
    this.cargarProveedores();
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
  }

  // ======== MÉTODOS EXISTENTES (cargar, buscar, eliminar, marcar deteriorado, alertas, etc.) ========

  cargarLotes(): void {
    this.cargando = true;
    this.loteService.listar().subscribe({
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
      },
      error: (err) => {
        console.error(err);
        this.cargando = false;
      }
    });
  }

  aplicarFiltros(): void {
    let filtrados = this.lotes;
    if (this.filtroEstado) {
      filtrados = filtrados.filter(l => l.estado === this.filtroEstado);
    }
    this.lotesFiltrados = filtrados;
  }

  buscar(): void {
    if (this.filtroProducto.trim()) {
      const id = Number(this.filtroProducto);
      if (!isNaN(id)) {
        this.loteService.listarPorProducto(id).subscribe({
          next: (data) => { 
            this.lotes = data; 
            this.aplicarFiltros(); 
          },
          error: (err) => console.error(err)
        });
      } else {
        this.loteService.buscarPorLote(this.filtroProducto).subscribe({
          next: (data) => { 
            this.lotes = [data]; 
            this.aplicarFiltros(); 
          },
          error: (err) => {
            if (err.status === 404) { 
              this.lotes = []; 
              this.aplicarFiltros(); 
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

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este lote?')) {
      this.loteService.eliminar(id).subscribe({
        next: () => {
          this.lotes = this.lotes.filter(l => l.id !== id);
          this.aplicarFiltros();
          this.calcularAlertas();
        },
        error: (err) => console.error(err)
      });
    }
  }

  marcarDeteriorado(id: number): void {
    if (confirm('¿Marcar este lote como deteriorado?')) {
      this.loteService.marcarDeteriorado(id).subscribe({
        next: () => {
          const lote = this.lotes.find(l => l.id === id);
          if (lote) lote.estado = 'deteriorado';
          this.aplicarFiltros();
          this.calcularAlertas();
        },
        error: (err) => alert(err.error?.mensaje || 'Error al marcar deteriorado')
      });
    }
  }

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
    const map: any = {
      'activo': 'bg-success text-white',
      'deteriorado': 'bg-warning text-dark',
      'vencido': 'bg-danger text-white',
      'agotado': 'bg-secondary text-white'
    };
    return map[estado] || 'bg-secondary text-white';
  }

  // ======== CARGAR DATOS PARA SELECTS ========
  cargarProductos(): void {
    this.productoService.listar().subscribe({
      next: (data) => this.productos = data,
      error: (err) => console.error('Error cargando productos:', err)
    });
  }

  cargarProveedores(): void {
    this.proveedorService.listar().subscribe({
      next: (data) => this.proveedores = data,
      error: (err) => console.error('Error cargando proveedores:', err)
    });
  }

  // ======== OFFCANVAS CREAR ========
  abrirOffcanvasCrear(): void {
    this.nuevoLote = { 
      productoId: null, 
      lote: '', 
      fechaVencimiento: '', 
      cantidad: 1, 
      precioVenta: 0, 
      estado: 'activo' 
    };
    this.offcanvasCrearAbierto = true;
  }

  cerrarOffcanvasCrear(): void {
    this.offcanvasCrearAbierto = false;
    this.guardando = false;
  }

  guardarLote(): void {
    if (!this.nuevoLote.productoId || !this.nuevoLote.lote || !this.nuevoLote.fechaVencimiento || this.nuevoLote.cantidad <= 0) {
      alert('Completa todos los campos obligatorios.');
      return;
    }
    this.guardando = true;
    this.loteService.crear(this.nuevoLote).subscribe({
      next: () => {
        this.guardando = false;
        this.cerrarOffcanvasCrear();
        this.cargarLotes();
        alert('Lote creado exitosamente');
      },
      error: (err) => {
        this.guardando = false;
        alert(err.error?.mensaje || 'Error al crear lote');
      }
    });
  }

  // ======== MODAL EDITAR ========
  abrirModalEditar(id: number): void {
    this.loteIdEditar = id;
    this.modalEditarAbierto = true;
    this.cargando = true;
    this.loteService.obtener(id).subscribe({
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
    this.guardandoEditar = false;
    this.formEditar.reset();
  }

  actualizarLote(): void {
    if (this.formEditar.invalid) {
      alert('Por favor completa todos los campos correctamente.');
      return;
    }
    this.guardandoEditar = true;
    this.loteService.actualizar(this.loteIdEditar, this.formEditar.value).subscribe({
      next: () => {
        this.guardandoEditar = false;
        this.cerrarModalEditar();
        this.cargarLotes();
        alert('Lote actualizado exitosamente');
      },
      error: (err) => {
        this.guardandoEditar = false;
        alert(err.error?.mensaje || 'Error al actualizar lote');
      }
    });
  }

  // ======== MODAL AJUSTAR STOCK ========
  abrirModalAjustar(id: number): void {
    this.loteIdAjustar = id;
    this.modalAjustarAbierto = true;
    this.cargando = true;
    this.loteService.obtener(id).subscribe({
      next: (data: Lote) => {
        this.loteAjustar = data;
        this.formAjustar.patchValue({ 
          tipoMovimiento: 'ajuste', 
          cantidad: '', 
          observacion: '' 
        });
        this.cargando = false;
      },
      error: (err) => {
        console.error(err);
        this.cargando = false;
        this.cerrarModalAjustar();
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
      alert('Ingresa una cantidad válida.');
      return;
    }
    const request = {
      cantidad: this.formAjustar.value.cantidad,
      tipoMovimiento: this.formAjustar.value.tipoMovimiento,
      observacion: this.formAjustar.value.observacion || '',
      usuarioId: this.usuarioId
    };
    this.guardandoAjustar = true;
    this.loteService.ajustarStock(this.loteIdAjustar, request).subscribe({
      next: (response) => {
        this.guardandoAjustar = false;
        this.cerrarModalAjustar();
        this.cargarLotes();
        alert(response.mensaje || 'Stock ajustado correctamente');
      },
      error: (err) => {
        this.guardandoAjustar = false;
        alert(err.error?.mensaje || 'Error al ajustar stock');
      }
    });
  }
}