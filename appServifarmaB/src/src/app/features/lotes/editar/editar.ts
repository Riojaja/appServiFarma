import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoteService } from '../../../core/services/lote';
import { ProductoService } from '../../../core/services/producto';
import { ProveedorService } from '../../../core/services/proveedor';
import { Lote } from '../../../core/models/lote.model';
import { Producto } from '../../../core/models/producto.model';
import { Proveedor } from '../../../core/models/proveedor.model';

@Component({
  selector: 'app-editar-lote',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './editar.html',
  styleUrls: ['./editar.css']
})
export class EditarComponent implements OnInit {
  form: FormGroup;
  enviando: boolean = false;
  loteId!: number;
  cargando: boolean = true;
  productos: Producto[] = [];
  proveedores: Proveedor[] = [];

  constructor(
    private fb: FormBuilder,
    private loteService: LoteService,
    private productoService: ProductoService,
    private proveedorService: ProveedorService,
    private router: Router,
    private route: ActivatedRoute
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
    this.loteId = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarProductos();
    this.cargarProveedores();
    this.cargarLote();
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

  cargarLote(): void {
    this.cargando = true;
    this.loteService.obtener(this.loteId).subscribe({
      next: (data: Lote) => {
        this.form.patchValue({
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
      error: (err: any) => {
        console.error('Error al cargar lote:', err);
        this.cargando = false;
        this.router.navigate(['/lotes']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.loteService.actualizar(this.loteId, this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/lotes']);
      },
      error: (err: any) => {
        console.error('Error al actualizar:', err);
        this.enviando = false;
        if (err.error?.mensaje) {
          alert(err.error.mensaje);
        }
      }
    });
  }
}