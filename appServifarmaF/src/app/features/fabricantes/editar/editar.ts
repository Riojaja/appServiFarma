import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FabricanteService } from '../../../core/services/fabricante';
import { Fabricante } from '../../../core/models/fabricante.model';

@Component({
  selector: 'app-editar-fabricante',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './editar.html',
  styleUrls: ['./editar.css']
})
export class EditarComponent implements OnInit {
  form: FormGroup;
  enviando: boolean = false;
  fabricanteId!: number;
  cargando: boolean = true;

  constructor(
    private fb: FormBuilder,
    private fabricanteService: FabricanteService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(150)]],
      contacto: ['', [Validators.maxLength(100)]],
      telefono: ['', [Validators.maxLength(20)]],
      email: ['', [Validators.email, Validators.maxLength(100)]]
    });
  }

  ngOnInit(): void {
    this.fabricanteId = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarFabricante();
  }

  cargarFabricante(): void {
    this.cargando = true;
    this.fabricanteService.obtener(this.fabricanteId).subscribe({
      next: (data: Fabricante) => {
        this.form.patchValue({
          nombre: data.nombre,
          contacto: data.contacto,
          telefono: data.telefono,
          email: data.email
        });
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar fabricante:', err);
        this.cargando = false;
        this.router.navigate(['/fabricantes']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.fabricanteService.actualizar(this.fabricanteId, this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/fabricantes']);
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