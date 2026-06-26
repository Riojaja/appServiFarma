import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { VentaService } from '../../../core/services/venta';

@Component({
  selector: 'app-anular-venta',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './anular.html',
  styleUrls: ['./anular.css']
})
export class AnularComponent implements OnInit {
  ventaId!: number;
  cargando: boolean = false;
  mensaje: string = '';
  error: string = '';

  constructor(
    private ventaService: VentaService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.ventaId = Number(this.route.snapshot.paramMap.get('id'));
    this.confirmarAnulacion();
  }

  confirmarAnulacion(): void {
    if (!this.ventaId) {
      this.error = 'ID de venta no válido.';
      return;
    }

    if (!confirm('¿Está seguro de anular esta venta? Esta acción no se puede deshacer.')) {
      this.router.navigate(['/ventas/listar']);
      return;
    }

    this.cargando = true;
    this.ventaService.anular(this.ventaId).subscribe({
      next: () => {
        this.cargando = false;
        this.mensaje = 'Venta anulada exitosamente.';
        setTimeout(() => this.router.navigate(['/ventas/listar']), 2000);
      },
      error: (err) => {
        this.cargando = false;
        this.error = err.error?.mensaje || 'Error al anular la venta.';
        console.error('Error:', err);
      }
    });
  }
}