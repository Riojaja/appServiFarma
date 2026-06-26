import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductoService } from '../../../core/services/producto';
import { CategoriaService } from '../../../core/services/categoria';
import { FabricanteService } from '../../../core/services/fabricante';
import { Producto } from '../../../core/models/producto.model';
import { Categoria } from '../../../core/models/categoria.model';
import { Fabricante } from '../../../core/models/fabricante.model';

@Component({
  selector: 'app-editar-producto',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './editar.html',
  styleUrls: ['./editar.css']
})
export class EditarComponent implements OnInit {
  form: FormGroup;
  enviando: boolean = false;
  productoId!: number;
  cargando: boolean = true;
  categorias: Categoria[] = [];
  fabricantes: Fabricante[] = [];

  constructor(
    private fb: FormBuilder,
    private productoService: ProductoService,
    private categoriaService: CategoriaService,
    private fabricanteService: FabricanteService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
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
    this.productoId = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarCategorias();
    this.cargarFabricantes();
    this.cargarProducto();
  }

  cargarCategorias(): void {
    this.categoriaService.listar().subscribe({
      next: (data: Categoria[]) => this.categorias = data,
      error: (err: any) => console.error('Error al cargar categorías:', err)
    });
  }

  cargarFabricantes(): void {
    this.fabricanteService.listar().subscribe({
      next: (data: Fabricante[]) => this.fabricantes = data,
      error: (err: any) => console.error('Error al cargar fabricantes:', err)
    });
  }

  cargarProducto(): void {
    this.cargando = true;
    this.productoService.obtener(this.productoId).subscribe({
      next: (data: Producto) => {
        this.form.patchValue({
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
      error: (err: any) => {
        console.error('Error al cargar producto:', err);
        this.cargando = false;
        this.router.navigate(['/productos']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.productoService.actualizar(this.productoId, this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/productos']);
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