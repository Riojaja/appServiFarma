import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { VentaService } from '../../../core/services/venta';
import { ProductoService } from '../../../core/services/producto';
import { ClienteService } from '../../../core/services/cliente';
import { ReniecService, ReniecResponse } from '../../../core/services/reniec';
import { AuthService } from '../../../core/auth';
import { Venta, VentaRequest, DetalleVentaRequest } from '../../../core/models/venta.model';
import { Producto } from '../../../core/models/producto.model';
import { Cliente } from '../../../core/models/cliente.model';

// ===== NUEVAS IMPORTACIONES PARA PDF E IMPRESIÓN =====
import * as jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

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

  // ======== MODAL DE REGISTRO ========
  modalRegistroAbierto: boolean = false;
  formRegistro: FormGroup;
  productos: Producto[] = [];
  clientes: Cliente[] = [];
  carrito: { productoId: number, nombre: string, cantidad: number, precio: number }[] = [];
  total: number = 0;
  productoSeleccionado: number = 0;
  cantidadProducto: number = 1;
  enviandoRegistro: boolean = false;
  errorRegistro: string = '';

  // ======== CAMPOS DE CLIENTE ========
  dniBusqueda: string = '';
  clienteEncontrado: Cliente | null = null;
  nombreCliente: string = '';
  telefonoCliente: string = '';     // NUEVO
  emailCliente: string = '';        // NUEVO
  buscarClienteCargando: boolean = false;
  datosDesdeReniec: boolean = false;

  // ======== MODAL DE DETALLE ========
  modalDetalleAbierto: boolean = false;
  ventaSeleccionada: Venta | null = null;

  // ======== MODAL DE OPCIONES DE BOLETA (NUEVO) ========
  modalBoletaAbierto: boolean = false;
  ventaRecienCreada: Venta | null = null;
  enviandoCorreo: boolean = false;

  constructor(
    private ventaService: VentaService,
    private productoService: ProductoService,
    private clienteService: ClienteService,
    private reniecService: ReniecService,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.formRegistro = this.fb.group({
      medioPago: ['', Validators.required],
      codigoAutorizacion: ['']
    });
  }

  ngOnInit(): void {
    const rol = this.authService.getRol();
    this.esAdministrador = rol === 'administrador';
    const id = this.authService.getUsuarioId();
    this.usuarioIdActual = id ? Number(id) : null;

    this.cargarVentas();
    this.cargarProductos();
    this.cargarClientes();
  }

  // ======== MÉTODOS DEL LISTADO ========
  cargarVentas(): void {
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
      },
      error: (err: any) => {
        console.error('Error al cargar ventas:', err);
        this.cargando = false;
      }
    });
  }

  aplicarFiltros(): void {
    this.ventasFiltradas = this.ventas.filter(v => {
      if (this.filtros.fechaInicio && new Date(v.fecha) < new Date(this.filtros.fechaInicio)) return false;
      if (this.filtros.fechaFin && new Date(v.fecha) > new Date(this.filtros.fechaFin)) return false;
      if (this.filtros.cliente && !v.clienteId?.toString().includes(this.filtros.cliente)) return false;
      if (this.filtros.usuario && !v.usuarioId?.toString().includes(this.filtros.usuario)) return false;
      if (this.filtros.estado && v.estado !== this.filtros.estado) return false;
      return true;
    });
  }

  limpiarFiltros(): void {
    this.filtros = { fechaInicio: '', fechaFin: '', cliente: '', usuario: '', estado: '' };
    this.aplicarFiltros();
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
    this.formRegistro.reset({ medioPago: '' });
    this.carrito = [];
    this.total = 0;
    this.productoSeleccionado = 0;
    this.cantidadProducto = 1;
    this.errorRegistro = '';
    this.dniBusqueda = '';
    this.clienteEncontrado = null;
    this.nombreCliente = '';
    this.telefonoCliente = '';     // NUEVO
    this.emailCliente = '';        // NUEVO
    this.datosDesdeReniec = false;
    this.modalRegistroAbierto = true;
  }

  cerrarModalRegistro(): void {
    this.modalRegistroAbierto = false;
    this.enviandoRegistro = false;
  }

  cargarProductos(): void {
    this.productoService.listar().subscribe({
      next: (data) => {
        this.productos = data;
        console.log('✅ Productos cargados:', this.productos.length);
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

  // ======== BÚSQUEDA DE CLIENTE CON RENIEC ========
  buscarCliente(): void {
    if (!this.dniBusqueda || this.dniBusqueda.length < 8) {
      this.clienteEncontrado = null;
      this.nombreCliente = '';
      this.telefonoCliente = '';
      this.emailCliente = '';
      return;
    }

    this.buscarClienteCargando = true;

    // 1. Buscar en la base de datos local
    this.clienteService.buscarPorDocumento(this.dniBusqueda).subscribe({
      next: (cliente: Cliente) => {
        this.clienteEncontrado = cliente;
        this.nombreCliente = cliente.nombre || '';
        this.telefonoCliente = cliente.telefono || '';
        this.emailCliente = cliente.email || '';
        this.buscarClienteCargando = false;
      },
      error: (err: any) => {
        if (err.status === 404) {
          this.consultarReniec(this.dniBusqueda);
        } else {
          console.error('Error al buscar cliente:', err);
          this.buscarClienteCargando = false;
          alert('Error al buscar cliente en la base de datos.');
        }
      }
    });
  }

  consultarReniec(dni: string): void {
    this.reniecService.consultarPorDni(dni).subscribe({
      next: (response: ReniecResponse) => {
        this.buscarClienteCargando = false;
        if (response.success && response.data) {
          this.nombreCliente = response.data.nombreCompleto ||
            `${response.data.nombres} ${response.data.apellidoPaterno} ${response.data.apellidoMaterno}`;
          this.datosDesdeReniec = true;
          // Limpiamos campos de contacto para que el vendedor los ingrese
          this.telefonoCliente = '';
          this.emailCliente = '';
        } else {
          this.clienteEncontrado = null;
          this.nombreCliente = '';
          this.telefonoCliente = '';
          this.emailCliente = '';
          alert(response.message || 'DNI no encontrado en RENIEC. Ingresa el nombre manualmente.');
        }
      },
      error: (err) => {
        this.buscarClienteCargando = false;
        console.error('Error al consultar RENIEC:', err);

        let mensaje = 'Error al consultar RENIEC. ';
        if (err.status === 429) {
          mensaje += 'Límite de consultas alcanzado. Intenta mañana.';
        } else if (err.status === 401 || err.status === 403) {
          mensaje += 'Credenciales inválidas. Contacta al administrador.';
        } else {
          mensaje += 'Ingresa el nombre manualmente.';
        }
        alert(mensaje);
        this.nombreCliente = '';
        this.telefonoCliente = '';
        this.emailCliente = '';
      }
    });
  }

  // ======== AGREGAR PRODUCTOS AL CARRITO ========
  agregarProducto(): void {
    console.log('📦 Agregar producto llamado');
    console.log('productoSeleccionado:', this.productoSeleccionado);
    console.log('cantidadProducto:', this.cantidadProducto);
    console.log('productos disponibles:', this.productos.length);

    const productoId = this.productoSeleccionado;
    const cantidad = this.cantidadProducto || 1;

    if (!productoId || productoId === 0) {
      console.warn('❌ No se seleccionó producto');
      alert('Selecciona un producto primero.');
      return;
    }

    const producto = this.productos.find(p => p.id === productoId);
    if (!producto) {
      console.warn('❌ Producto no encontrado en la lista:', productoId);
      alert('Producto no encontrado.');
      return;
    }

    console.log('✅ Producto encontrado:', producto.nombre);

    // Verificar si ya existe en el carrito
    const existente = this.carrito.find(item => item.productoId === productoId);
    if (existente) {
      existente.cantidad += cantidad;
      console.log('➕ Cantidad actualizada:', existente.cantidad);
    } else {
      this.carrito.push({
        productoId: producto.id!,
        nombre: producto.nombre,
        cantidad: cantidad,
        precio: producto.precioVentaActual
      });
      console.log('🆕 Producto agregado al carrito');
    }

    this.calcularTotal();
    this.productoSeleccionado = 0;
    this.cantidadProducto = 1;
    console.log('📊 Carrito actual:', this.carrito);
  }

  eliminarDelCarrito(index: number): void {
    this.carrito.splice(index, 1);
    this.calcularTotal();
  }

  calcularTotal(): void {
    this.total = this.carrito.reduce((sum, item) => sum + (item.cantidad * item.precio), 0);
  }

  // ======== REGISTRAR VENTA CON CLIENTE ========
  registrarVenta(): void {
    if (this.formRegistro.invalid || this.carrito.length === 0) {
      this.errorRegistro = 'Debe agregar al menos un producto y completar los datos obligatorios.';
      return;
    }

    this.enviandoRegistro = true;
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
        codigoAutorizacion: this.formRegistro.value.codigoAutorizacion || undefined,
        detalles: detalles
      };

      this.ventaService.registrar(request).subscribe({
        next: (ventaCreada: Venta) => {
          this.enviandoRegistro = false;
          this.cerrarModalRegistro();
          this.cargarVentas();
          // Guardar la venta creada y mostrar opciones de boleta
          this.ventaRecienCreada = ventaCreada;
          this.mostrarOpcionesBoleta(ventaCreada);
        },
        error: (err) => {
          this.enviandoRegistro = false;
          this.errorRegistro = err.error?.mensaje || 'Error al registrar la venta';
          console.error('Error:', err);
        }
      });
    };

    // Si no hay DNI, venta anónima
    if (!documentoNumero) {
      continuarRegistro(undefined);
      return;
    }

    // Si ya tenemos cliente encontrado (de BD), usamos su ID
    if (this.clienteEncontrado) {
      continuarRegistro(this.clienteEncontrado.id);
      return;
    }

    // Si no existe en BD (pero tenemos nombre de RENIEC o manual), lo creamos
    if (!this.nombreCliente.trim()) {
      this.errorRegistro = 'Debe ingresar el nombre del cliente o verificar el DNI.';
      this.enviandoRegistro = false;
      return;
    }

    // Crear cliente con los datos (de RENIEC o manual)
    const nuevoCliente: Cliente = {
      documentoTipo: 'DNI',
      documentoNumero: documentoNumero,
      nombre: this.nombreCliente.trim(),
      telefono: this.telefonoCliente || '',   // NUEVO
      email: this.emailCliente || ''          // NUEVO
    };

    this.clienteService.crear(nuevoCliente).subscribe({
      next: (cliente) => {
        continuarRegistro(cliente.id);
      },
      error: (err) => {
        this.enviandoRegistro = false;
        this.errorRegistro = err.error?.mensaje || 'Error al crear el cliente.';
        console.error('Error:', err);
      }
    });
  }

  // ======== OPCIONES DE BOLETA (NUEVO) ========
  mostrarOpcionesBoleta(venta: Venta): void {
    this.ventaRecienCreada = venta;
    this.modalBoletaAbierto = true;
  }

  cerrarModalBoleta(): void {
    this.modalBoletaAbierto = false;
    this.ventaRecienCreada = null;
    this.enviandoCorreo = false;
  }

  // ======== IMPRIMIR BOLETA ========
  imprimirBoleta(): void {
    if (!this.ventaRecienCreada) {
      alert('No hay datos de la venta para imprimir.');
      return;
    }

    // Generar el contenido HTML de la boleta
    const content = document.createElement('div');
    content.innerHTML = `
      <div style="width: 300px; font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ccc; margin: 0 auto;">
        <h2 style="text-align: center; color: #2c3e50;">BOLETA DE VENTA</h2>
        <hr>
        <p><strong>N° Venta:</strong> ${this.ventaRecienCreada.id}</p>
        <p><strong>Fecha:</strong> ${new Date(this.ventaRecienCreada.fecha).toLocaleString()}</p>
        <p><strong>Cliente:</strong> ${this.nombreCliente || 'Anónimo'}</p>
        <p><strong>Vendedor:</strong> ${this.usuarioIdActual || 'Sistema'}</p>
        <hr>
        <table style="width: 100%; border-collapse: collapse;">
          <thead>
            <tr style="background-color: #f8f9fa;">
              <th style="padding: 5px; text-align: left;">Producto</th>
              <th style="padding: 5px; text-align: center;">Cant.</th>
              <th style="padding: 5px; text-align: right;">Precio</th>
              <th style="padding: 5px; text-align: right;">Subtotal</th>
            </tr>
          </thead>
          <tbody>
            ${this.carrito.map(item => `
              <tr>
                <td style="padding: 5px;">${item.nombre}</td>
                <td style="padding: 5px; text-align: center;">${item.cantidad}</td>
                <td style="padding: 5px; text-align: right;">S/ ${item.precio.toFixed(2)}</td>
                <td style="padding: 5px; text-align: right;">S/ ${(item.cantidad * item.precio).toFixed(2)}</td>
              </tr>
            `).join('')}
          </tbody>
          <tfoot>
            <tr style="border-top: 2px solid #000;">
              <td colspan="3" style="padding: 10px; text-align: right; font-weight: bold;">TOTAL</td>
              <td style="padding: 10px; text-align: right; font-weight: bold;">S/ ${this.total.toFixed(2)}</td>
            </tr>
          </tfoot>
        </table>
        <hr>
        <p style="text-align: center; font-size: 12px; color: #7f8c8d;">¡Gracias por su compra!</p>
      </div>
    `;

    // Usar html2canvas para capturar y generar PDF
    html2canvas(content, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#ffffff'
    }).then((canvas) => {
      const imgData = canvas.toDataURL('image/png');
      const doc = new jsPDF.jsPDF('p', 'mm', 'a4');
      const imgWidth = 190;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      doc.addImage(imgData, 'PNG', 10, 10, imgWidth, imgHeight);
      doc.autoPrint();
      const pdfBlob = doc.output('blob');
      const url = URL.createObjectURL(pdfBlob);
      window.open(url, '_blank');
      URL.revokeObjectURL(url);
    }).catch((error) => {
      console.error('Error al generar la boleta:', error);
      alert('Error al generar la boleta para imprimir.');
    });
  }

  // ======== ENVIAR BOLETA POR CORREO ========
  enviarBoletaCorreo(): void {
    // 1. Verificar que la venta existe
    if (!this.ventaRecienCreada) {
      alert('No hay datos de la venta para enviar.');
      return;
    }

    // 2. Verificar que tiene ID
    if (!this.ventaRecienCreada.id) {
      alert('La venta no tiene un ID válido.');
      return;
    }

    // 3. Verificar correo
    const correoDestino = this.emailCliente?.trim() || '';
    if (!correoDestino) {
      alert('El cliente no tiene un correo registrado. Por favor, ingrese un correo.');
      return;
    }

    this.enviandoCorreo = true;

    // 4. Llamar al servicio (ahora con id asegurado)
    this.ventaService.enviarBoletaCorreo(this.ventaRecienCreada.id, correoDestino).subscribe({
      next: (response: any) => {
        this.enviandoCorreo = false;
        alert('Boleta enviada exitosamente al correo ' + correoDestino);
        this.cerrarModalBoleta();
      },
      error: (err: any) => {
        this.enviandoCorreo = false;
        console.error('Error al enviar boleta:', err);
        alert('Error al enviar la boleta: ' + (err.error?.mensaje || 'Intente nuevamente.'));
      }
    });
  }

  // ======== MODAL DE DETALLE ========
  abrirModalDetalle(venta: Venta): void {
    this.ventaSeleccionada = venta;
    this.modalDetalleAbierto = true;
  }

  cerrarModalDetalle(): void {
    this.modalDetalleAbierto = false;
    this.ventaSeleccionada = null;
  }

  irAnular(id: number): void {
    this.router.navigate(['/ventas/anular', id]);
  }
}