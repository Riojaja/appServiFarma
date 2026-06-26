import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoriaService } from '../../../core/services/categoria';
import { Categoria } from '../../../core/models/categoria.model';

@Component({
  selector: 'app-editar-categoria',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './editar.html',
  styleUrls: ['./editar.css']
})
export class EditarComponent implements OnInit {
  form: FormGroup;
  enviando: boolean = false;
  categoriaId!: number;
  cargando: boolean = true;

  constructor(
    private fb: FormBuilder,
    private categoriaService: CategoriaService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(80)]],
      descripcion: ['', [Validators.maxLength(200)]]
    });
  }

  ngOnInit(): void {
    this.categoriaId = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarCategoria();
  }

  cargarCategoria(): void {
    this.cargando = true;
    this.categoriaService.obtener(this.categoriaId).subscribe({
      next: (data: Categoria) => {
        this.form.patchValue({
          nombre: data.nombre,
          descripcion: data.descripcion
        });
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar categoría:', err);
        this.cargando = false;
        this.router.navigate(['/categorias']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.categoriaService.actualizar(this.categoriaId, this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/categorias']);
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