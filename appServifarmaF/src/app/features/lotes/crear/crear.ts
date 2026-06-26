import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoteService } from '../../../core/services/lote';
import { ProductoService } from '../../../core/services/producto';
import { ProveedorService } from '../../../core/services/proveedor';
import { Producto } from '../../../core/models/producto.model';
import { Proveedor } from '../../../core/models/proveedor.model';

@Component({
  selector: 'app-crear-lote',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './crear.html',
  styleUrls: ['./crear.css']
})
export class CrearComponent implements OnInit {
  form: FormGroup;
  enviando: boolean = false;
  productos: Producto[] = [];
  proveedores: Proveedor[] = [];

  constructor(
    private fb: FormBuilder,
    private loteService: LoteService,
    private productoService: ProductoService,
    private proveedorService: ProveedorService,
    private router: Router
  ) {
    this.form = this.fb.group({
      productoId: ['', Validators.required],
      proveedorId: ['', Validators.required],
      lote: ['', [Validators.required, Validators.maxLength(50)]],
      fechaIngreso: ['', Validators.required],
      fechaVencimiento: ['', Validators.required],
      cantidad: ['', [Validators.required, Validators.min(1)]],
      precioCompra: ['', [Validators.required, Validators.min(0.01)]],
      precioVenta: ['', [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.cargarProductos();
    this.cargarProveedores();
  }

  cargarProductos(): void {
    this.productoService.listar().subscribe({
      next: (data: Producto[]) => this.productos = data,
      error: (err: any) => console.error('Error al cargar productos:', err)
    });
  }

  cargarProveedores(): void {
    this.proveedorService.listar().subscribe({
      next: (data: Proveedor[]) => this.proveedores = data,
      error: (err: any) => console.error('Error al cargar proveedores:', err)
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.loteService.crear(this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/lotes']);
      },
      error: (err: any) => {
        console.error('Error al crear lote:', err);
        this.enviando = false;
        if (err.error?.mensaje) {
          alert(err.error.mensaje);
        }
      }
    });
  }
}