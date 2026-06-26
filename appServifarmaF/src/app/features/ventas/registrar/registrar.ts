import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { VentaService } from '../../../core/services/venta';
import { ProductoService } from '../../../core/services/producto';
import { ClienteService } from '../../../core/services/cliente';
import { AuthService } from '../../../core/auth';
import { Producto } from '../../../core/models/producto.model';
import { Cliente } from '../../../core/models/cliente.model';
import { VentaRequest, DetalleVentaRequest } from '../../../core/models/venta.model';

@Component({
  selector: 'app-registrar-venta',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './registrar.html',
  styleUrls: ['./registrar.css']
})
export class RegistrarComponent implements OnInit {
  form: FormGroup;
  productos: Producto[] = [];
  clientes: Cliente[] = [];
  carrito: { productoId: number, nombre: string, cantidad: number, precio: number }[] = [];
  total: number = 0;
  usuarioId: number = 0;
  enviando: boolean = false;
  errorMessage: string = '';

  productoSeleccionado: number = 0;
  cantidadProducto: number = 1;

  constructor(
    private fb: FormBuilder,
    private ventaService: VentaService,
    private productoService: ProductoService,
    private clienteService: ClienteService,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      clienteId: [''],
      medioPago: ['', Validators.required],
      codigoAutorizacion: ['']
    });
  }

  ngOnInit(): void {
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
    this.cargarProductos();
    this.cargarClientes();
  }

  cargarProductos(): void {
    this.productoService.listar().subscribe({
      next: (data) => this.productos = data,
      error: (err) => console.error('Error al cargar productos:', err)
    });
  }

  cargarClientes(): void {
    this.clienteService.listar().subscribe({
      next: (data) => this.clientes = data,
      error: (err) => console.error('Error al cargar clientes:', err)
    });
  }

  onProductoChange(): void {
    // No es necesario hacer nada aquí
  }

  agregarProducto(): void {
    const productoId = this.productoSeleccionado;
    const cantidad = this.cantidadProducto || 1;
    if (!productoId) return;

    const producto = this.productos.find(p => p.id === productoId);
    if (!producto) return;

    const existente = this.carrito.find(item => item.productoId === productoId);
    if (existente) {
      existente.cantidad += cantidad;
    } else {
      this.carrito.push({
        productoId: producto.id!,
        nombre: producto.nombre,
        cantidad: cantidad,
        precio: producto.precioVentaActual // <--- Campo agregado
      });
    }
    this.calcularTotal();
    this.productoSeleccionado = 0;
    this.cantidadProducto = 1;
  }

  eliminarDelCarrito(index: number): void {
    this.carrito.splice(index, 1);
    this.calcularTotal();
  }

  calcularTotal(): void {
    this.total = this.carrito.reduce((sum, item) => sum + (item.cantidad * item.precio), 0);
  }

  onSubmit(): void {
    if (this.form.invalid || this.carrito.length === 0) {
      this.errorMessage = 'Debe agregar al menos un producto y completar los datos obligatorios.';
      return;
    }

    this.enviando = true;
    this.errorMessage = '';

    const detalles: DetalleVentaRequest[] = this.carrito.map(item => ({
      productoId: item.productoId,
      cantidad: item.cantidad
    }));

    const request: VentaRequest = {
      usuarioId: this.usuarioId,
      clienteId: this.form.value.clienteId || undefined,
      medioPago: this.form.value.medioPago,
      codigoAutorizacion: this.form.value.codigoAutorizacion || undefined,
      detalles: detalles
    };

    this.ventaService.registrar(request).subscribe({
      next: () => {
        this.enviando = false;
        alert('Venta registrada exitosamente.');
        this.router.navigate(['/ventas/listar']);
      },
      error: (err) => {
        this.enviando = false;
        this.errorMessage = err.error?.mensaje || 'Error al registrar la venta';
        console.error('Error:', err);
      }
    });
  }
}