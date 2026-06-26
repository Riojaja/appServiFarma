import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FabricanteService } from '../../../core/services/fabricante';

@Component({
  selector: 'app-crear-fabricante',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './crear.html',
  styleUrls: ['./crear.css']
})
export class CrearComponent {
  form: FormGroup;
  enviando: boolean = false;

  constructor(
    private fb: FormBuilder,
    private fabricanteService: FabricanteService,
    private router: Router
  ) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(150)]],
      contacto: ['', [Validators.maxLength(100)]],
      telefono: ['', [Validators.maxLength(20)]],
      email: ['', [Validators.email, Validators.maxLength(100)]]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.fabricanteService.crear(this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/fabricantes']);
      },
      error: (err: any) => {
        console.error('Error al crear fabricante:', err);
        this.enviando = false;
        if (err.error?.mensaje) {
          alert(err.error.mensaje);
        }
      }
    });
  }
}