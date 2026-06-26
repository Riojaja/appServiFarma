import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CajaService } from '../../../core/services/caja';
import { AuthService } from '../../../core/auth';

@Component({
  selector: 'app-apertura-caja',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './apertura.html',
  styleUrls: ['./apertura.css']
})
export class AperturaComponent implements OnInit {
  form: FormGroup;
  enviando: boolean = false;
  errorMessage: string = '';
  existeCajaAbierta: boolean = false;
  usuarioId: number = 0;

  constructor(
    private fb: FormBuilder,
    private cajaService: CajaService,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      montoApertura: ['', [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
    this.verificarEstado();
  }

  verificarEstado(): void {
    this.cajaService.existeCajaAbierta().subscribe({
      next: (existe) => {
        this.existeCajaAbierta = existe;
        if (existe) {
          this.errorMessage = 'Ya existe una caja abierta. Debes cerrarla antes de abrir otra.';
        }
      },
      error: (err) => {
        console.error('Error al verificar caja:', err);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.enviando || this.existeCajaAbierta) {
      return;
    }

    this.enviando = true;
    this.errorMessage = '';

    const request = {
      usuarioAperturaId: this.usuarioId,
      montoApertura: this.form.value.montoApertura
    };

    this.cajaService.abrir(request).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/caja/estado']);
      },
      error: (err) => {
        this.enviando = false;
        this.errorMessage = err.error?.mensaje || 'Error al abrir la caja';
        console.error('Error:', err);
      }
    });
  }
}