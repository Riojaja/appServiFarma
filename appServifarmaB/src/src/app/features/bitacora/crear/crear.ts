import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BitacoraService } from '../../../core/services/bitacora';
import { AuthService } from '../../../core/auth';

@Component({
  selector: 'app-crear-mensaje-bitacora',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './crear.html',
  styleUrls: ['./crear.css']
})
export class CrearComponent {
  form: FormGroup;
  enviando: boolean = false;
  errorMessage: string = '';
  usuarioId: number = 0;

  constructor(
    private fb: FormBuilder,
    private bitacoraService: BitacoraService,
    private authService: AuthService,
    private router: Router
  ) {
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
    this.form = this.fb.group({
      mensaje: ['', [Validators.required, Validators.maxLength(2000)]],
      tipo: ['novedad', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.errorMessage = '';

    const request = {
      ...this.form.value,
      usuarioId: this.usuarioId
    };

    this.bitacoraService.crear(request).subscribe({
      next: () => {
        this.enviando = false;
        alert('Mensaje creado exitosamente.');
        this.router.navigate(['/bitacora/listar']);
      },
      error: (err) => {
        this.enviando = false;
        this.errorMessage = err.error?.mensaje || 'Error al crear el mensaje';
        console.error('Error:', err);
      }
    });
  }
}