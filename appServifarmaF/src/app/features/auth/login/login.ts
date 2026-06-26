import { Component } from '@angular/core';
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

  constructor(private authService: AuthService, private router: Router) { }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.isLoading = true;

    this.authService.login(this.loginData).subscribe({
      next: () => {
        this.isLoading = false;
        const rol = this.authService.getRol();
        if (rol?.toUpperCase() === 'ADMIN') {
          this.router.navigate(['/dashboard']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.mensaje || 'Usuario o contraseña incorrectos';
        console.error('Error de login:', err);
      }
    });
  }
}