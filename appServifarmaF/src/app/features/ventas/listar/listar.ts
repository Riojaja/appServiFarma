import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { VentaService } from '../../../core/services/venta';
import { ProductoService } from '../../../core/services/producto';
import { ClienteService } from '../../../core/services/cliente';
import { ReniecService, ReniecResponse } from '../../../core/services/reniec';
import { AuthService } from '../../../core/auth';
import { CajaService } from '../../../core/services/caja';
import { Venta, VentaRequest, DetalleVentaRequest } from '../../../core/models/venta.model';
import { Producto } from '../../../core/models/producto.model';
import { Cliente } from '../../../core/models/cliente.model';
import { environment } from '../../../../environments/environment';
import Swal from 'sweetalert2';

// ===== IMPORTACIONES PARA PDF =====
import * as jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

// ===== DATOS DE LA BOTICA =====
const NOMBRE_BOTICA = 'ServiFarma';
const DIRECCION_BOTICA = 'Av. sinchi roca, Abancay - Apurimac';
const RUC_BOTICA = '10474101156';
const TELEFONO_BOTICA = '992859321';
const LOGO_BOTICA = '/logoServifarma.jpeg';


@Component({
  selector: 'app-listar-ventas',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  // ======== PROPIEDADES DEL LISTADO ========
  ventas: Venta[] = [];
  ventasFiltradas: Venta[] = [];
  ventasPaginadas: Venta[] = [];
  public Math = Math;
  filtros = {
    fechaInicio: '',
    fechaFin: '',
    cliente: '',
    usuario: '',
    estado: ''
  };
  cargando: boolean = false;
  esAdministrador: boolean = false;
  usuarioIdActual: number | null = null;
  cargandoDetalles: boolean = false;

  // ======== PAGINACIÓN ========
  itemsPorPagina: number = 10;
  paginaActual: number = 1;
  totalPaginas: number = 1;

  // ======== CACHÉ DE CONSULTAS DNI ========
  private dniCache = new Map<string, ReniecResponse>();

  // ======== MODAL DE REGISTRO ========
  modalRegistroAbierto: boolean = false;
  formRegistro: FormGroup;
  productos: Producto[] = [];
  clientes: Cliente[] = [];
  carrito: { productoId: number, nombre: string, cantidad: number, precio: number }[] = [];
  total: number = 0;
  cantidadProducto: number = 1;
  enviandoRegistro: boolean = false;
  errorRegistro: string = '';

  // ======== CAMPOS DE CLIENTE ========
  dniBusqueda: string = '';
  clienteEncontrado: Cliente | null = null;
  nombreCliente: string = '';
  telefonoCliente: string = '';
  emailCliente: string = '';
  buscarClienteCargando: boolean = false;
  datosDesdeReniec: boolean = false;

  // ======== CAJA ========
  cajaId: number | null = null;

  // ======== MODAL DE DETALLE ========
  modalDetalleAbierto: boolean = false;
  ventaSeleccionada: Venta | null = null;

  // ======== MODAL DE OPCIONES DE BOLETA ========
  modalBoletaAbierto: boolean = false;
  ventaRecienCreada: Venta | null = null;
  enviandoCorreo: boolean = false;
  generandoPDF: boolean = false;

  // ======== MODAL DE SELECCIÓN DE PRODUCTOS ========
  modalProductosAbierto: boolean = false;
  productosModal: Producto[] = [];
  filtroProductoModal: string = '';
  cargandoProductosModal: boolean = false;
  agregandoProducto: boolean = false;

  // ======== ANULAR VENTA ========
  anulando: boolean = false;

  constructor(
    private ventaService: VentaService,
    private productoService: ProductoService,
    private clienteService: ClienteService,
    private reniecService: ReniecService,
    private cajaService: CajaService,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.formRegistro = this.fb.group({
      medioPago: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const rol = this.authService.getRol();
    this.esAdministrador = rol?.toLowerCase() === 'admin' || rol?.toLowerCase() === 'administrador';
    const id = this.authService.getUsuarioId();
    this.usuarioIdActual = id ? Number(id) : null;

    this.cargarVentas();
    this.cargarProductos();
    this.cargarClientes();
  }

  // ======== MÉTODOS DEL LISTADO ========
  cargarVentas(): void {
    if (this.cargando) return;

    this.cargando = true;
    this.ventaService.listar().subscribe({
      next: (data: Venta[]) => {
        if (!this.esAdministrador && this.usuarioIdActual) {
          this.ventas = data.filter(v => v.usuarioId === this.usuarioIdActual);
        } else {
          this.ventas = data;
        }
        this.aplicarFiltros();
        this.cargando = false;
        this.cdr.detectChanges();
        console.log('✅ Ventas cargadas:', this.ventas.length);
      },
      error: (err: any) => {
        console.error('❌ Error al cargar ventas:', err);
        this.cargando = false;
        this.cdr.detectChanges();
        Swal.fire({
          title: 'Error',
          text: 'No se pudieron cargar las ventas',
          icon: 'error',
          customClass: { popup: 'swal-farmaceutico' }
        });
      }
    });
  }

  aplicarFiltros(): void {
    let filtradas = this.ventas;

    if (this.filtros.fechaInicio) {
      filtradas = filtradas.filter(v => new Date(v.fecha) >= new Date(this.filtros.fechaInicio));
    }
    if (this.filtros.fechaFin) {
      filtradas = filtradas.filter(v => new Date(v.fecha) <= new Date(this.filtros.fechaFin));
    }
    if (this.filtros.cliente) {
      filtradas = filtradas.filter(v => v.clienteId?.toString().includes(this.filtros.cliente));
    }
    if (this.filtros.usuario) {
      filtradas = filtradas.filter(v => v.usuarioId?.toString().includes(this.filtros.usuario));
    }
    if (this.filtros.estado) {
      filtradas = filtradas.filter(v => v.estado === this.filtros.estado);
    }

    this.ventasFiltradas = filtradas;
    this.paginaActual = 1;
    this.totalPaginas = Math.ceil(this.ventasFiltradas.length / this.itemsPorPagina);
    this.actualizarPaginacion();
    this.cdr.detectChanges();
  }

  limpiarFiltros(): void {
    this.filtros = { fechaInicio: '', fechaFin: '', cliente: '', usuario: '', estado: '' };
    this.aplicarFiltros();
  }

  // ======== PAGINACIÓN ========
  actualizarPaginacion(): void {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    const fin = inicio + this.itemsPorPagina;
    this.ventasPaginadas = this.ventasFiltradas.slice(inicio, fin);
    this.cdr.detectChanges();
  }

  siguientePagina(): void {
    if (this.paginaActual < this.totalPaginas) {
      this.paginaActual++;
      this.actualizarPaginacion();
    }
  }

  anteriorPagina(): void {
    if (this.paginaActual > 1) {
      this.paginaActual--;
      this.actualizarPaginacion();
    }
  }

  // ======== OBTENER NOMBRE DE CLIENTE ========
  getNombreCliente(clienteId: number | undefined): string {
    if (!clienteId) return 'Anónimo';
    const cliente = this.clientes.find(c => c.id === clienteId);
    return cliente ? cliente.nombre : 'Anónimo';
  }

  getEstadoBadge(estado: string): string {
    const map: { [key: string]: string } = {
      'completada': 'bg-success',
      'anulada': 'bg-danger',
      'pendiente': 'bg-warning'
    };
    return map[estado] || 'bg-secondary';
  }

  // ======== MODAL DE REGISTRO ========
  abrirModalRegistro(): void {
    if (this.modalRegistroAbierto) return;
    this.formRegistro.reset({ medioPago: '' });
    this.carrito = [];
    this.total = 0;
    this.cantidadProducto = 1;
    this.errorRegistro = '';
    this.dniBusqueda = '';
    this.clienteEncontrado = null;
    this.nombreCliente = '';
    this.telefonoCliente = '';
    this.emailCliente = '';
    this.datosDesdeReniec = false;
    this.cajaId = null;

    this.cargarCajaAbierta();
    this.modalRegistroAbierto = true;
    this.cdr.detectChanges();
  }

  cerrarModalRegistro(): void {
    if (this.enviandoRegistro) return;
    this.modalRegistroAbierto = false;
    this.enviandoRegistro = false;
    this.cdr.detectChanges();
  }

  // ======== OBTENER CAJA ABIERTA ========
  cargarCajaAbierta(): void {
    this.cajaService.obtenerCajaAbierta().subscribe({
      next: (caja) => {
        if (caja && caja.id) {
          this.cajaId = caja.id;
        } else {
          this.errorRegistro = 'No hay una caja abierta.';
          Swal.fire({
            title: 'Atención',
            text: 'No hay una caja abierta. Debe abrir la caja primero.',
            icon: 'warning',
            customClass: { popup: 'swal-farmaceutico' }
          });
          this.cajaId = null;
        }
      },
      error: () => {
        this.errorRegistro = 'No se pudo verificar la caja.';
        Swal.fire({
          title: 'Error',
          text: 'No se pudo verificar la caja. Asegúrate de tener una caja abierta.',
          icon: 'error',
          customClass: { popup: 'swal-farmaceutico' }
        });
        this.cajaId = null;
      }
    });
  }

  cargarProductos(): void {
    this.productoService.listar().subscribe({
      next: (data) => {
        this.productos = data;
        this.productosModal = data;
      },
      error: (err) => {
        console.error('❌ Error al cargar productos:', err);
      }
    });
  }

  cargarClientes(): void {
    this.clienteService.listar().subscribe({
      next: (data) => this.clientes = data,
      error: (err) => console.error('Error al cargar clientes:', err)
    });
  }

  // ======== BÚSQUEDA DE CLIENTE CON CACHÉ ========
  buscarCliente(): void {
    if (this.buscarClienteCargando) return;

    if (!this.dniBusqueda || this.dniBusqueda.length < 8) {
      this.clienteEncontrado = null;
      this.nombreCliente = '';
      this.telefonoCliente = '';
      this.emailCliente = '';
      return;
    }

    // Verificar caché
    if (this.dniCache.has(this.dniBusqueda)) {
      const cached = this.dniCache.get(this.dniBusqueda)!;
      if (cached.success && cached.data) {
        this.nombreCliente = cached.data.nombreCompleto;
        this.datosDesdeReniec = true;
        this.telefonoCliente = '';
        this.emailCliente = '';
        Swal.fire({
          title: 'Datos de RENIEC (caché)',
          text: `Nombre: ${this.nombreCliente}`,
          icon: 'info',
          timer: 1500,
          showConfirmButton: false,
          customClass: { popup: 'swal-farmaceutico' }
        });
        return;
      }
    }

    this.buscarClienteCargando = true;

    this.clienteService.buscarPorDocumento(this.dniBusqueda).subscribe({
      next: (cliente: Cliente) => {
        this.clienteEncontrado = cliente;
        this.nombreCliente = cliente.nombre || '';
        this.telefonoCliente = cliente.telefono || '';
        this.emailCliente = cliente.email || '';
        this.buscarClienteCargando = false;
        Swal.fire({
          title: 'Cliente encontrado',
          text: `Cliente registrado: ${cliente.nombre}`,
          icon: 'success',
          timer: 2000,
          showConfirmButton: false,
          customClass: { popup: 'swal-farmaceutico' }
        });
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        if (err.status === 404) {
          this.consultarReniec(this.dniBusqueda);
        } else {
          console.error('Error al buscar cliente:', err);
          this.buscarClienteCargando = false;
          Swal.fire({
            title: 'Error',
            text: 'Error al buscar cliente en la base de datos.',
            icon: 'error',
            customClass: { popup: 'swal-farmaceutico' }
          });
        }
      }
    });
  }

  consultarReniec(dni: string): void {
    this.reniecService.consultarPorDni(dni).subscribe({
      next: (response: ReniecResponse) => {
        this.buscarClienteCargando = false;
        // Guardar en caché
        this.dniCache.set(dni, response);
        if (response.success && response.data) {
          this.nombreCliente = response.data.nombreCompleto ||
            `${response.data.nombres} ${response.data.apellidoPaterno} ${response.data.apellidoMaterno}`;
          this.datosDesdeReniec = true;
          this.telefonoCliente = '';
          this.emailCliente = '';
          Swal.fire({
            title: 'Datos de RENIEC',
            text: `Nombre: ${this.nombreCliente}`,
            icon: 'info',
            customClass: { popup: 'swal-farmaceutico' }
          });
        } else {
          this.clienteEncontrado = null;
          this.nombreCliente = '';
          this.telefonoCliente = '';
          this.emailCliente = '';
          Swal.fire({
            title: 'No encontrado',
            text: response.message || 'DNI no encontrado en RENIEC. Ingresa el nombre manualmente.',
            icon: 'warning',
            customClass: { popup: 'swal-farmaceutico' }
          });
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.buscarClienteCargando = false;
        console.error('Error al consultar RENIEC:', err);
        let mensaje = 'Error al consultar RENIEC. Ingresa el nombre manualmente.';
        if (err.status === 429) mensaje = 'Límite de consultas alcanzado. Intenta mañana.';
        else if (err.status === 401 || err.status === 403) mensaje = 'Credenciales inválidas. Contacta al administrador.';
        Swal.fire({
          title: 'Error',
          text: mensaje,
          icon: 'error',
          customClass: { popup: 'swal-farmaceutico' }
        });
        this.nombreCliente = '';
        this.telefonoCliente = '';
        this.emailCliente = '';
        this.cdr.detectChanges();
      }
    });
  }

  // ======== FUNCIONES DE IMAGEN ========
  public obtenerUrlCompleta(ruta: string): string {
    if (!ruta) return '';
    if (ruta.startsWith('http://') || ruta.startsWith('https://')) return ruta;
    const hostBase = environment.apiUrl.replace(/\/api\/?$/, '');
    const rutaLimpia = ruta.startsWith('/') ? ruta : `/${ruta}`;
    return `${hostBase}${rutaLimpia}`;
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/placeholder-producto.svg';
  }

  // ======== MODAL DE SELECCIÓN DE PRODUCTOS ========
  abrirModalProductos(): void {
    if (this.modalProductosAbierto) return;
    this.modalProductosAbierto = true;
    this.filtroProductoModal = '';
    this.productosModal = this.productos.slice();
    this.cdr.detectChanges();
  }

  cerrarModalProductos(): void {
    this.modalProductosAbierto = false;
    this.cdr.detectChanges();
  }

  buscarProductosModal(): void {
    const term = this.filtroProductoModal.toLowerCase().trim();
    if (!term) {
      this.productosModal = this.productos.slice();
      return;
    }
    this.productosModal = this.productos.filter(p =>
      p.nombre.toLowerCase().includes(term)
    );
    this.cdr.detectChanges();
  }

  seleccionarProductoModal(producto: Producto): void {
    if (this.agregandoProducto) return;
    this.agregandoProducto = true;

    const cantidad = this.cantidadProducto || 1;
    const existente = this.carrito.find(item => item.productoId === producto.id!);
    if (existente) {
      existente.cantidad += cantidad;
    } else {
      this.carrito.push({
        productoId: producto.id!,
        nombre: producto.nombre,
        cantidad: cantidad,
        precio: producto.precioVentaActual || 0
      });
    }
    this.calcularTotal();
    this.cerrarModalProductos();
    this.cantidadProducto = 1;
    this.agregandoProducto = false;
    this.cdr.detectChanges();
  }

  eliminarDelCarrito(index: number): void {
    this.carrito.splice(index, 1);
    this.calcularTotal();
    this.cdr.detectChanges();
  }

  calcularTotal(): void {
    this.total = this.carrito.reduce((sum, item) => sum + (item.cantidad * item.precio), 0);
  }

  // ======== REGISTRAR VENTA CON CLIENTE ========
  registrarVenta(): void {
    if (this.enviandoRegistro) return;

    if (this.formRegistro.invalid) {
      this.formRegistro.markAllAsTouched();
      Swal.fire({
        title: 'Formulario incompleto',
        text: 'Selecciona un método de pago.',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }
    if (this.carrito.length === 0) {
      Swal.fire({
        title: 'Carrito vacío',
        text: 'Agrega al menos un producto.',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }
    if (!this.cajaId) {
      Swal.fire({
        title: 'Caja cerrada',
        text: 'Debe abrir la caja antes de registrar una venta.',
        icon: 'error',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    this.enviandoRegistro = true;

    Swal.fire({
      title: '¿Confirmar venta?',
      text: `Total: S/ ${this.total.toFixed(2)}`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#0d9488',
      cancelButtonColor: '#dc2626',
      confirmButtonText: 'Sí, registrar',
      cancelButtonText: 'Cancelar',
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (!result.isConfirmed) {
        this.enviandoRegistro = false;
        return;
      }

      this.errorRegistro = '';
      const documentoNumero = this.dniBusqueda.trim();

      const continuarRegistro = (clienteId?: number) => {
        const detalles: DetalleVentaRequest[] = this.carrito.map(item => ({
          productoId: item.productoId,
          cantidad: item.cantidad
        }));

        const request: VentaRequest = {
          usuarioId: this.usuarioIdActual!,
          clienteId: clienteId,
          medioPago: this.formRegistro.value.medioPago,
          cajaId: this.cajaId!,
          detalles: detalles
        };

        this.ventaService.registrar(request).subscribe({
          next: (ventaCreada: Venta) => {
            this.enviandoRegistro = false;
            this.cerrarModalRegistroForzado();
            this.cargarVentas();
            this.ventaRecienCreada = ventaCreada;
            Swal.fire({
              title: '¡Venta registrada!',
              text: `Venta #${ventaCreada.id} registrada exitosamente`,
              icon: 'success',
              timer: 1500,
              showConfirmButton: false,
              customClass: { popup: 'swal-farmaceutico' }
            });
            setTimeout(() => {
              this.mostrarOpcionesBoleta(ventaCreada);
            }, 500);
          },
          error: (err) => {
            this.enviandoRegistro = false;
            console.error('❌ Error al registrar venta:', err);
            const mensaje = err.error?.mensaje || err.error?.message || 'Error al registrar la venta';
            this.errorRegistro = mensaje;
            Swal.fire({
              title: 'Error',
              text: mensaje,
              icon: 'error',
              customClass: { popup: 'swal-farmaceutico' }
            });
            this.cdr.detectChanges();
          }
        });
      };

      if (!documentoNumero) {
        continuarRegistro(undefined);
        return;
      }

      if (this.clienteEncontrado) {
        continuarRegistro(this.clienteEncontrado.id);
        return;
      }

      if (!this.nombreCliente.trim()) {
        Swal.fire({
          title: 'Datos incompletos',
          text: 'Ingresa el nombre del cliente o verifica el DNI.',
          icon: 'warning',
          customClass: { popup: 'swal-farmaceutico' }
        });
        this.enviandoRegistro = false;
        return;
      }

      const nuevoCliente: Cliente = {
        documentoTipo: 'DNI',
        documentoNumero: documentoNumero,
        nombre: this.nombreCliente.trim(),
        telefono: this.telefonoCliente || '',
        email: this.emailCliente || ''
      };

      this.clienteService.crear(nuevoCliente).subscribe({
        next: (cliente) => {
          continuarRegistro(cliente.id);
        },
        error: (err) => {
          this.enviandoRegistro = false;
          this.errorRegistro = err.error?.mensaje || 'Error al crear el cliente.';
          Swal.fire({
            title: 'Error',
            text: this.errorRegistro,
            icon: 'error',
            customClass: { popup: 'swal-farmaceutico' }
          });
          this.cdr.detectChanges();
        }
      });
    });
  }

  private cerrarModalRegistroForzado(): void {
    this.modalRegistroAbierto = false;
    this.cdr.detectChanges();
  }

  // ======== OPCIONES DE BOLETA ========
  mostrarOpcionesBoleta(venta: Venta): void {
    this.ventaRecienCreada = venta;
    this.modalBoletaAbierto = true;
    this.generandoPDF = false;
    this.enviandoCorreo = false;
    this.cdr.detectChanges();
  }

  cerrarModalBoleta(): void {
    if (this.enviandoCorreo || this.generandoPDF) return;
    this.modalBoletaAbierto = false;
    this.ventaRecienCreada = null;
    this.enviandoCorreo = false;
    this.generandoPDF = false;
    this.cargandoDetalles = false; // ✅ Resetear
    this.cdr.detectChanges();
  }

  private cerrarModalBoletaForzado(): void {
    this.modalBoletaAbierto = false;
    this.ventaRecienCreada = null;
    this.enviandoCorreo = false;
    this.generandoPDF = false;
    this.cargandoDetalles = false; // ✅ Resetear
    this.cdr.detectChanges();
  }

  // ============================================================
  // REIMPRESIÓN DE BOLETA (para ventas ya realizadas)
  // ============================================================
  reimprimirBoleta(venta: Venta): void {
    if (!venta || !venta.id) {
      Swal.fire({
        title: 'Error',
        text: 'No se pudo identificar la venta.',
        icon: 'error',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    // Resetear y abrir modal con spinner
    this.carrito = [];
    this.total = 0;
    this.ventaRecienCreada = venta;
    this.nombreCliente = this.getNombreCliente(venta.clienteId) || 'Público en general';

    // 🔥 OBTENER EL EMAIL DEL CLIENTE
    if (venta.clienteId) {
      this.clienteService.obtener(venta.clienteId).subscribe({
        next: (cliente) => {
          this.emailCliente = cliente.email || '';
          this.cdr.detectChanges();
        },
        error: () => {
          this.emailCliente = '';
          this.cdr.detectChanges();
        }
      });
    } else {
      this.emailCliente = '';
    }

    this.cargandoDetalles = true;
    this.modalBoletaAbierto = true;
    this.generandoPDF = false;
    this.enviandoCorreo = false;
    this.cdr.detectChanges();

    // Cargar detalles de la venta
    this.ventaService.obtener(venta.id).subscribe({
      next: (ventaCompleta: Venta) => {
        this.cargandoDetalles = false;
        if (!ventaCompleta.detalles || ventaCompleta.detalles.length === 0) {
          Swal.fire({
            title: 'Sin productos',
            text: 'Esta venta no tiene productos asociados.',
            icon: 'warning',
            customClass: { popup: 'swal-farmaceutico' }
          });
          this.cerrarModalBoleta();
          return;
        }

        // Reconstruir carrito
        this.carrito = ventaCompleta.detalles.map((d: any) => ({
          productoId: d.productoId || d.loteId,
          nombre: d.productoNombre || d.nombre || 'Producto',
          cantidad: d.cantidad || 0,
          precio: d.precioUnitario || d.precio || 0
        }));
        this.total = ventaCompleta.total || 0;
        this.ventaRecienCreada = ventaCompleta;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.cargandoDetalles = false;
        console.error('Error al obtener detalles:', err);
        Swal.fire({
          title: 'Error',
          text: 'No se pudieron cargar los detalles de la venta. Intente nuevamente.',
          icon: 'error',
          customClass: { popup: 'swal-farmaceutico' }
        });
        this.cerrarModalBoleta();
      }
    });
  }

  // ============================================================
  // PLANTILLA ÚNICA DE LA BOLETA
  // ============================================================
  private construirHtmlBoleta(venta: Venta): string {
    const fecha = venta.fecha ? new Date(venta.fecha) : new Date();
    const subtotal = this.total / 1.18;
    const igv = this.total - subtotal;
    const medioPago = (this.formRegistro.value.medioPago || 'efectivo').toUpperCase();

    const filas = this.carrito.map(item => `
      <tr>
        <td style="padding:4px 2px;text-align:center;">${item.cantidad}</td>
        <td style="padding:4px 2px;">${item.nombre}</td>
        <td style="padding:4px 2px;text-align:right;">${item.precio.toFixed(2)}</td>
        <td style="padding:4px 2px;text-align:right;">${(item.cantidad * item.precio).toFixed(2)}</td>
      </tr>
    `).join('');

    return `
      <div style="width:300px;font-family:'Courier New',monospace;padding:18px;background:#ffffff;color:#1e293b;">
        <div style="text-align:center;margin-bottom:10px;">
          <img src="${LOGO_BOTICA}" style="max-width:70px;max-height:70px;object-fit:contain;margin-bottom:6px;" />
          <div style="font-size:1.15rem;font-weight:bold;letter-spacing:1px;color:#0d9488;">${NOMBRE_BOTICA}</div>
          <div style="font-size:0.7rem;color:#475569;">${DIRECCION_BOTICA}</div>
          <div style="font-size:0.7rem;color:#475569;">RUC: ${RUC_BOTICA} · Telf: ${TELEFONO_BOTICA}</div>
        </div>

        <div style="border-top:1px dashed #94a3b8;margin:8px 0;"></div>

        <div style="font-size:0.78rem;line-height:1.5;">
          <div><strong>Ticket:</strong> #${String(venta.id ?? 0).padStart(6, '0')}</div>
          <div><strong>Fecha:</strong> ${fecha.toLocaleString('es-PE')}</div>
          <div><strong>Cliente:</strong> ${this.nombreCliente || 'Público en general'}</div>
          <div><strong>Vendedor:</strong> ${this.usuarioIdActual ?? 'Sistema'}</div>
        </div>

        <div style="border-top:1px dashed #94a3b8;margin:8px 0;"></div>

        <table style="width:100%;border-collapse:collapse;font-size:0.75rem;">
          <thead>
            <tr style="border-bottom:1px solid #1e293b;">
              <th style="text-align:center;padding:4px 2px;">Cant.</th>
              <th style="text-align:left;padding:4px 2px;">Descripción</th>
              <th style="text-align:right;padding:4px 2px;">P.Unit</th>
              <th style="text-align:right;padding:4px 2px;">Importe</th>
            </tr>
          </thead>
          <tbody>${filas}</tbody>
        </table>

        <div style="border-top:1px dashed #94a3b8;margin:8px 0;"></div>

        <div style="font-size:0.78rem;">
          <div style="display:flex;justify-content:space-between;"><span>Subtotal</span><span>S/ ${subtotal.toFixed(2)}</span></div>
          <div style="display:flex;justify-content:space-between;"><span>IGV (18%)</span><span>S/ ${igv.toFixed(2)}</span></div>
          <div style="display:flex;justify-content:space-between;font-weight:bold;font-size:0.95rem;border-top:1px solid #1e293b;margin-top:4px;padding-top:4px;">
            <span>TOTAL</span><span>S/ ${this.total.toFixed(2)}</span>
          </div>
        </div>

        <div style="border-top:1px dashed #94a3b8;margin:8px 0;"></div>

        <div style="font-size:0.78rem;"><strong>Medio de pago:</strong> ${medioPago}</div>

        <div style="text-align:center;margin-top:14px;">
          <div style="font-size:0.85rem;font-weight:bold;color:#0d9488;">¡Gracias por su compra!</div>
          <div style="font-size:0.68rem;color:#94a3b8;margin-top:2px;">Conserve este comprobante</div>
        </div>
      </div>
    `;
  }

  // ============================================================
  // EXPORTAR A PDF
  // ============================================================
  exportarPDF(): void {
    if (this.generandoPDF) return;

    if (!this.ventaRecienCreada) {
      Swal.fire({
        title: 'Error',
        text: 'No hay datos de la venta para exportar.',
        icon: 'error',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    this.generandoPDF = true;

    const content = document.createElement('div');
    content.innerHTML = this.construirHtmlBoleta(this.ventaRecienCreada);
    content.style.position = 'absolute';
    content.style.left = '-9999px';
    content.style.top = '0';
    content.style.width = '300px';
    document.body.appendChild(content);

    html2canvas(content, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#ffffff',
      logging: false
    }).then((canvas) => {
      const imgData = canvas.toDataURL('image/png');
      const doc = new jsPDF.jsPDF('p', 'mm', 'a4');
      const imgWidth = 100;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;

      doc.addImage(imgData, 'PNG', 10, 10, imgWidth, imgHeight);

      const pdfBlob = doc.output('blob');
      const url = URL.createObjectURL(pdfBlob);
      window.open(url, '_blank');
      URL.revokeObjectURL(url);

      document.body.removeChild(content);
      // ✅ Cerrar modal y limpiar estado
      this.cerrarModalBoletaForzado();
      this.cdr.detectChanges();
    }).catch((error) => {
      console.error('Error al generar el PDF:', error);
      Swal.fire({
        title: 'Error',
        text: 'Error al generar el PDF.',
        icon: 'error',
        customClass: { popup: 'swal-farmaceutico' }
      });
      if (content.parentNode) document.body.removeChild(content);
      this.generandoPDF = false;
      this.cerrarModalBoletaForzado();
      this.cdr.detectChanges();
    });
  }

  // ======== ENVIAR BOLETA POR CORREO ========
  enviarBoletaCorreo(): void {
    if (this.enviandoCorreo) return;

    if (!this.ventaRecienCreada) {
      Swal.fire({
        title: 'Error',
        text: 'No hay datos de la venta para enviar.',
        icon: 'error',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }
    if (!this.ventaRecienCreada.id) {
      Swal.fire({
        title: 'Error',
        text: 'La venta no tiene un ID válido.',
        icon: 'error',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    const correoDestino = this.emailCliente?.trim() || '';
    if (!correoDestino) {
      Swal.fire({
        title: 'Sin correo',
        text: 'El cliente no tiene correo registrado. No se puede enviar.',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    this.enviandoCorreo = true;
    this.cdr.detectChanges();

    Swal.fire({
      title: 'Enviando boleta...',
      text: `Se enviará al correo: ${correoDestino}`,
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading(),
      customClass: { popup: 'swal-farmaceutico' }
    });

    this.ventaService.enviarBoletaCorreo(this.ventaRecienCreada.id, correoDestino).subscribe({
      next: () => {
        this.enviandoCorreo = false;
        Swal.fire({
          title: '¡Enviado!',
          text: `Boleta enviada a ${correoDestino}`,
          icon: 'success',
          customClass: { popup: 'swal-farmaceutico' }
        });
        this.cerrarModalBoletaForzado();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.enviandoCorreo = false;
        console.error('Error al enviar boleta:', err);
        Swal.fire({
          title: 'Error',
          text: err.error?.mensaje || 'Error al enviar la boleta.',
          icon: 'error',
          customClass: { popup: 'swal-farmaceutico' }
        });
        // El modal permanece abierto para reintentar
        this.cdr.detectChanges();
      }
    });
  }

  // ======== MODAL DE DETALLE ========
  abrirModalDetalle(venta: Venta): void {
    if (this.modalDetalleAbierto) return;
    this.ventaSeleccionada = venta;
    this.modalDetalleAbierto = true;
    this.cdr.detectChanges();
  }

  cerrarModalDetalle(): void {
    this.modalDetalleAbierto = false;
    this.ventaSeleccionada = null;
    this.cdr.detectChanges();
  }

  irAnular(id: number): void {
    if (this.anulando) return;
    this.anulando = true;

    Swal.fire({
      title: '¿Anular venta?',
      text: 'Esta acción no se puede deshacer.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, anular',
      cancelButtonText: 'Cancelar',
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (result.isConfirmed) {
        this.router.navigate(['/ventas/anular', id]);
      } else {
        this.anulando = false;
      }
    });
  }
}