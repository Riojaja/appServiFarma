import { Component, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
  imports: [CommonModule, FormsModule],
})
export class LoginComponent {
  loginData = {
    usuario: '',
    contrasena: ''
  };
  errorMessage: string = '';
  isLoading: boolean = false;
  showPassword: boolean = false;

  private autoOcultarTimeout: any;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.ocultarError();

    if (!this.loginData.usuario.trim() || !this.loginData.contrasena.trim()) {
      this.mostrarError('Ingresa tu usuario y contraseña.');
      return;
    }

    this.isLoading = true;

    this.authService.login(this.loginData).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;

        let mensaje: string;
        if (err.status === 0) {
          mensaje = 'No se pudo conectar con el servidor. Verifica tu conexión.';
        } else {
          mensaje = err.error?.mensaje || 'Usuario o contraseña incorrectos';
        }

        this.mostrarError(mensaje);
        console.error('Error de login:', err);
      }
    });
  }

  private mostrarError(mensaje: string): void {
    this.errorMessage = mensaje;
    this.cdr.detectChanges();

    clearTimeout(this.autoOcultarTimeout);
    this.autoOcultarTimeout = setTimeout(() => {
      this.errorMessage = '';
      this.cdr.detectChanges();
    }, 6000);
  }

  private ocultarError(): void {
    this.errorMessage = '';
    clearTimeout(this.autoOcultarTimeout);
  }
}