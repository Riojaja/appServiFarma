import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CajaService } from '../../../core/services/caja';
import { VentaService } from '../../../core/services/venta';
import { AuthService } from '../../../core/auth';
import { Caja } from '../../../core/models/caja.model';
import { Venta } from '../../../core/models/venta.model';

@Component({
  selector: 'app-listar-caja',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  // ======== ESTADO DE LA CAJA ========
  caja: Caja | null = null;
  existeCajaAbierta: boolean = false;
  cargando: boolean = false;
  errorMessage: string = '';
  usuarioId: number = 0;

  // ======== RESUMEN DE VENTAS (DATOS REALES) ========
  resumenVentas = {
    efectivo: 0,
    tarjeta: 0,
    transferencia: 0,
    yape: 0,
    total: 0
  };

  // ======== CIERRE DE CAJA ========
  montoEsperado: number = 0;
  montoContado: number = 0;
  diferencia: number | null = null;

  // ======== MODAL APERTURA ========
  modalAperturaAbierto: boolean = false;
  formApertura: FormGroup;
  enviandoApertura: boolean = false;
  errorApertura: string = '';

  // ======== MODAL CIERRE ========
  modalCierreAbierto: boolean = false;
  formCierre: FormGroup;
  enviandoCierre: boolean = false;
  errorCierre: string = '';

  constructor(
    private fb: FormBuilder,
    private cajaService: CajaService,
    private ventaService: VentaService,
    private authService: AuthService
  ) {
    this.formApertura = this.fb.group({
      montoApertura: ['', [Validators.required, Validators.min(0.01)]]
    });

    this.formCierre = this.fb.group({
      montoCierreDeclarado: ['', [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
    this.cargarEstado();
  }

  cargarEstado(): void {
    this.cargando = true;
    this.cajaService.obtenerCajaAbierta().subscribe({
      next: (data: Caja) => {
        this.caja = data;
        this.existeCajaAbierta = true;
        this.cargando = false;
        this.cargarResumenVentas();
      },
      error: (err: any) => {
        if (err.status === 404) {
          this.existeCajaAbierta = false;
          this.caja = null;
          this.resumenVentas = { efectivo: 0, tarjeta: 0, transferencia: 0, yape: 0, total: 0 };
          this.montoEsperado = 0;
        } else {
          this.errorMessage = 'Error al obtener el estado de la caja.';
        }
        this.cargando = false;
        console.error('Error:', err);
      }
    });
  }

  // ======== CARGAR RESUMEN DE VENTAS (DATOS REALES) ========
  cargarResumenVentas(): void {
    if (!this.caja || !this.caja.id) {
      console.warn('No hay caja abierta o ID inválido');
      this.resumenVentas = { efectivo: 0, tarjeta: 0, transferencia: 0, yape: 0, total: 0 };
      this.montoEsperado = 0;
      return;
    }

    // Obtener TODAS las ventas y filtrar por cajaId
    this.ventaService.listar().subscribe({
      next: (ventas: Venta[]) => {
        // Filtrar ventas de esta caja
        const ventasCaja = ventas.filter(v => v.cajaId === this.caja?.id);
        
        // Calcular totales por método de pago
        let totalEfectivo = 0;
        let totalTarjeta = 0;
        let totalTransferencia = 0;
        let totalYape = 0;
        let totalGeneral = 0;

        ventasCaja.forEach(venta => {
          const monto = venta.total || 0;
          totalGeneral += monto;
          
          switch (venta.medioPago) {
            case 'efectivo':
              totalEfectivo += monto;
              break;
            case 'tarjeta':
              totalTarjeta += monto;
              break;
            case 'transferencia':
              totalTransferencia += monto;
              break;
            case 'yape':
              totalYape += monto;
              break;
          }
        });

        this.resumenVentas = {
          efectivo: totalEfectivo,
          tarjeta: totalTarjeta,
          transferencia: totalTransferencia,
          yape: totalYape,
          total: totalGeneral
        };

        // Calcular monto esperado (monto inicial + ventas en efectivo)
        this.montoEsperado = (this.caja?.montoApertura || 0) + totalEfectivo;
      },
      error: (err: any) => {
        console.error('Error al cargar ventas:', err);
        this.resumenVentas = { efectivo: 0, tarjeta: 0, transferencia: 0, yape: 0, total: 0 };
        this.montoEsperado = this.caja?.montoApertura || 0;
      }
    });
  }

  // ======== CALCULAR DIFERENCIA ========
  calcularDiferencia(): void {
    if (this.montoContado && this.montoEsperado) {
      this.diferencia = this.montoContado - this.montoEsperado;
    } else {
      this.diferencia = null;
    }
  }

  // ======== MODALES ========
  abrirModalApertura(): void {
    this.formApertura.reset({ montoApertura: '' });
    this.errorApertura = '';
    this.modalAperturaAbierto = true;
  }

  cerrarModalApertura(): void {
    this.modalAperturaAbierto = false;
    this.enviandoApertura = false;
  }

  abrirModalCierre(): void {
    this.formCierre.reset({ montoCierreDeclarado: '' });
    this.errorCierre = '';
    this.modalCierreAbierto = true;
  }

  cerrarModalCierre(): void {
    this.modalCierreAbierto = false;
    this.enviandoCierre = false;
  }

  // ======== APERTURA ========
  abrirCaja(): void {
    if (this.formApertura.invalid || this.enviandoApertura) {
      return;
    }

    this.enviandoApertura = true;
    this.errorApertura = '';

    const request = {
      usuarioAperturaId: this.usuarioId,
      montoApertura: this.formApertura.value.montoApertura
    };

    this.cajaService.abrir(request).subscribe({
      next: () => {
        this.enviandoApertura = false;
        this.cerrarModalApertura();
        this.cargarEstado();
        alert('✅ Caja abierta exitosamente');
      },
      error: (err: any) => {
        this.enviandoApertura = false;
        this.errorApertura = err.error?.mensaje || 'Error al abrir la caja';
        console.error('Error:', err);
      }
    });
  }

  // ======== CIERRE ========
  cerrarCaja(): void {
    if (this.formCierre.invalid || this.enviandoCierre || !this.caja) {
      return;
    }

    this.enviandoCierre = true;
    this.errorCierre = '';

    const request = {
      usuarioCierreId: this.usuarioId,
      montoCierreDeclarado: this.formCierre.value.montoCierreDeclarado
    };

    this.cajaService.cerrar(request).subscribe({
      next: (response: any) => {
        this.enviandoCierre = false;
        this.cerrarModalCierre();
        this.cargarEstado();
        alert(
          `✅ Caja cerrada exitosamente!\n\n` +
          `Total ventas: S/ ${response.totalVentas?.toFixed(2) || '0.00'}\n` +
          `Monto declarado: S/ ${response.montoDeclarado?.toFixed(2) || '0.00'}\n` +
          `Diferencia: S/ ${response.diferencia?.toFixed(2) || '0.00'}`
        );
      },
      error: (err: any) => {
        this.enviandoCierre = false;
        this.errorCierre = err.error?.mensaje || 'Error al cerrar la caja';
        console.error('Error:', err);
      }
    });
  }

  // ======== FORMATO ========
  formatearMonto(valor: number): string {
    return 'S/ ' + (valor || 0).toFixed(2);
  }
}