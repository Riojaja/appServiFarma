import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DemandaService } from '../../../core/services/demanda';
import { AuthService } from '../../../core/auth';

@Component({
  selector: 'app-registrar-demanda',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './registrar.html',
  styleUrls: ['./registrar.css']
})
export class RegistrarComponent {
  form: FormGroup;
  enviando: boolean = false;
  errorMessage: string = '';
  usuarioId: number = 0;

  constructor(
    private fb: FormBuilder,
    private demandaService: DemandaService,
    private authService: AuthService,
    private router: Router
  ) {
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
    this.form = this.fb.group({
      productoSolicitado: ['', [Validators.required, Validators.maxLength(200)]],
      clienteDocumento: ['', [Validators.maxLength(20)]]
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

    this.demandaService.registrar(request).subscribe({
      next: () => {
        this.enviando = false;
        alert('Demanda registrada exitosamente.');
        this.router.navigate(['/demanda/listar']);
      },
      error: (err) => {
        this.enviando = false;
        this.errorMessage = err.error?.mensaje || 'Error al registrar la demanda';
        console.error('Error:', err);
      }
    });
  }
}