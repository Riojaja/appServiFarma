import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CajaService } from '../../../core/services/caja';
import { AuthService } from '../../../core/auth';

@Component({
  selector: 'app-cierre-caja',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './cierre.html',
  styleUrls: ['./cierre.css']
})
export class CierreComponent implements OnInit {
  form: FormGroup;
  enviando: boolean = false;
  errorMessage: string = '';
  cajaAbierta: any = null;
  totalVentas: number = 0;
  usuarioId: number = 0;

  constructor(
    private fb: FormBuilder,
    private cajaService: CajaService,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      montoCierreDeclarado: ['', [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
    this.cargarCajaAbierta();
  }

  cargarCajaAbierta(): void {
    this.cajaService.obtenerCajaAbierta().subscribe({
      next: (data) => {
        this.cajaAbierta = data;
        this.cargarTotalVentas();
      },
      error: (err) => {
        if (err.status === 404) {
          this.errorMessage = 'No hay una caja abierta para cerrar.';
        } else {
          this.errorMessage = 'Error al obtener la caja abierta.';
        }
        console.error('Error:', err);
      }
    });
  }

  cargarTotalVentas(): void {
    if (!this.cajaAbierta) return;
    this.cajaService.obtenerTotalVentas(this.cajaAbierta.id).subscribe({
      next: (total) => {
        this.totalVentas = total;
      },
      error: (err) => console.error('Error al obtener total de ventas:', err)
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.enviando || !this.cajaAbierta) {
      return;
    }

    this.enviando = true;
    this.errorMessage = '';

    const request = {
      usuarioCierreId: this.usuarioId,
      montoCierreDeclarado: this.form.value.montoCierreDeclarado
    };

    this.cajaService.cerrar(request).subscribe({
      next: (response) => {
        this.enviando = false;
        // Mostrar resumen del cierre
        alert(
          `Caja cerrada exitosamente!\n\n` +
          `Total ventas: S/ ${response.totalVentas.toFixed(2)}\n` +
          `Monto declarado: S/ ${response.montoDeclarado.toFixed(2)}\n` +
          `Diferencia: S/ ${response.diferencia.toFixed(2)}\n` +
          `Usuario cierre: ${response.usuarioCierre}`
        );
        this.router.navigate(['/caja/estado']);
      },
      error: (err) => {
        this.enviando = false;
        this.errorMessage = err.error?.mensaje || 'Error al cerrar la caja';
        console.error('Error:', err);
      }
    });
  }
}