import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoteService } from '../../../core/services/lote'; // Ajusta la ruta si es necesario
import { AuthService } from '../../../core/auth'; // <--- Importar AuthService

@Component({
  selector: 'app-ajustar-stock',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule
  ],
  templateUrl: './ajustar-stock.html',
  styleUrls: ['./ajustar-stock.css']
})
export class AjustarStockComponent implements OnInit {
  loteId!: number;
  lote: any = null;
  form: FormGroup;
  stockResultante: number | null = null;
  enviando: boolean = false;
  errorMessage: string = '';
  cargando: boolean = true;
  usuarioId: number = 0;  // <--- Agregar

  constructor(
    private fb: FormBuilder,
    private loteService: LoteService,
    private authService: AuthService,  // <--- Inyectar
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.form = this.fb.group({
      tipoMovimiento: ['', Validators.required],
      cantidad: ['', [Validators.required, Validators.min(1)]],
      observacion: ['']
    });
  }

  ngOnInit(): void {
    this.loteId = Number(this.route.snapshot.paramMap.get('id'));
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;  // <--- Obtener usuarioId
    this.cargarLote();
  }

  cargarLote(): void {
    this.cargando = true;
    this.loteService.obtener(this.loteId).subscribe({
      next: (data) => {
        this.lote = data;
        this.cargando = false;
        this.stockResultante = this.lote.cantidad;
      },
      error: (err) => {
        console.error('Error al cargar lote:', err);
        this.cargando = false;
        this.errorMessage = 'No se pudo cargar el lote.';
      }
    });
  }

  onCantidadChange(): void {
    const cantidad = this.form.get('cantidad')?.value || 0;
    const tipo = this.form.get('tipoMovimiento')?.value;
    if (!this.lote || !cantidad) {
      this.stockResultante = this.lote ? this.lote.cantidad : null;
      return;
    }
    if (tipo === 'ajuste') {
      this.stockResultante = this.lote.cantidad + cantidad;
    } else if (tipo === 'merma') {
      this.stockResultante = this.lote.cantidad - cantidad;
    } else {
      this.stockResultante = this.lote.cantidad;
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.errorMessage = '';

    // Construir el request SIN loteId (va en la URL)
    const request = {
      cantidad: this.form.value.cantidad,
      tipoMovimiento: this.form.value.tipoMovimiento,
      observacion: this.form.value.observacion || '',
      usuarioId: this.usuarioId  // <--- Agregar usuarioId
    };

    // Llamar al servicio real
    this.loteService.ajustarStock(this.loteId, request).subscribe({
      next: (response) => {
        this.enviando = false;
        alert(response.mensaje || 'Ajuste de stock realizado exitosamente.');
        this.router.navigate(['/lotes']);
      },
      error: (err) => {
        this.enviando = false;
        this.errorMessage = err.error?.mensaje || 'Error al ajustar stock.';
        console.error('Error:', err);
      }
    });
  }
}