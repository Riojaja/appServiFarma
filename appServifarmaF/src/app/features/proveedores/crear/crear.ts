import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProveedorService } from '../../../core/services/proveedor';

@Component({
  selector: 'app-crear-proveedor',
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
    private proveedorService: ProveedorService,
    private router: Router
  ) {
    this.form = this.fb.group({
      ruc: ['', [Validators.required, Validators.minLength(11), Validators.maxLength(11)]],
      razonSocial: ['', [Validators.required, Validators.maxLength(150)]],
      direccion: ['', [Validators.maxLength(200)]],
      telefono: ['', [Validators.maxLength(20)]],
      email: ['', [Validators.email, Validators.maxLength(100)]],
      contacto: ['', [Validators.maxLength(100)]],
      region: ['', [Validators.maxLength(50)]]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.proveedorService.crear(this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/proveedores']);
      },
      error: (err: any) => {
        console.error('Error al crear proveedor:', err);
        this.enviando = false;
        if (err.error?.mensaje) {
          alert(err.error.mensaje);
        }
      }
    });
  }
}