import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductoService } from '../../../core/services/producto';
import { CategoriaService } from '../../../core/services/categoria';
import { FabricanteService } from '../../../core/services/fabricante';
import { Categoria } from '../../../core/models/categoria.model';
import { Fabricante } from '../../../core/models/fabricante.model';

@Component({
  selector: 'app-crear-producto',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './crear.html',
  styleUrls: ['./crear.css']
})
export class CrearComponent implements OnInit {
  form: FormGroup;
  enviando: boolean = false;
  categorias: Categoria[] = [];
  fabricantes: Fabricante[] = [];

  constructor(
    private fb: FormBuilder,
    private productoService: ProductoService,
    private categoriaService: CategoriaService,
    private fabricanteService: FabricanteService,
    private router: Router
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
    this.cargarCategorias();
    this.cargarFabricantes();
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

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.productoService.crear(this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/productos']);
      },
      error: (err: any) => {
        console.error('Error al crear producto:', err);
        this.enviando = false;
        if (err.error?.mensaje) {
          alert(err.error.mensaje);
        }
      }
    });
  }
}