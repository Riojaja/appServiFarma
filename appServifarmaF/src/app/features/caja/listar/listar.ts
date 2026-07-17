import { Component, OnInit, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CajaService } from '../../../core/services/caja';
import { VentaService } from '../../../core/services/venta';
import { AuthService } from '../../../core/auth';
import { Caja } from '../../../core/models/caja.model';
import { Venta } from '../../../core/models/venta.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-listar-caja',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit, AfterViewInit {
  // ======== ESTADO DE LA CAJA ========
  caja: Caja | null = null;
  existeCajaAbierta: boolean = false;
  cargando: boolean = false;
  errorMessage: string = '';
  usuarioId: number = 0;
  private primeraCarga: boolean = true;

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
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.formApertura = this.fb.group({
      montoApertura: ['', [Validators.required, Validators.min(0.01)]]
    });

    this.formCierre = this.fb.group({
      montoCierreDeclarado: ['', [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    console.log('🔄 ngOnInit ejecutado');
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
    // La carga se hará en ngAfterViewInit para asegurar que la vista esté lista
  }

  ngAfterViewInit(): void {
    console.log('🔄 ngAfterViewInit ejecutado');
    if (this.primeraCarga) {
      this.primeraCarga = false;
      setTimeout(() => {
        this.cargarEstado();
      }, 100);
    }
  }

  cargarEstado(): void {
    if (this.cargando) return;

    console.log('🔄 Cargando estado de caja...');
    this.cargando = true;
    this.errorMessage = '';

    this.cajaService.obtenerCajaAbierta().subscribe({
      next: (data: Caja) => {
        this.caja = data;
        this.existeCajaAbierta = true;
        this.cargando = false;
        this.cdr.detectChanges();
        this.cargarResumenVentas();
      },
      error: (err: any) => {
        if (err.status === 404) {
          this.existeCajaAbierta = false;
          this.caja = null;
          this.resumenVentas = { efectivo: 0, tarjeta: 0, transferencia: 0, yape: 0, total: 0 };
          this.montoEsperado = 0;
          this.cargando = false;
          this.cdr.detectChanges();
        } else {
          this.errorMessage = 'Error al obtener el estado de la caja.';
          this.cargando = false;
          this.cdr.detectChanges();
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: this.errorMessage,
            confirmButtonColor: '#2563eb',
            customClass: { popup: 'swal-farmaceutico' }
          });
        }
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
      this.cdr.detectChanges();
      return;
    }

    this.ventaService.listar().subscribe({
      next: (ventas: Venta[]) => {
        const ventasCaja = ventas.filter(v => v.cajaId === this.caja?.id);
        
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

        this.montoEsperado = (this.caja?.montoApertura || 0) + totalEfectivo;
        this.cdr.detectChanges();
        console.log('✅ Resumen de ventas actualizado:', this.resumenVentas);
      },
      error: (err: any) => {
        console.error('Error al cargar ventas:', err);
        this.resumenVentas = { efectivo: 0, tarjeta: 0, transferencia: 0, yape: 0, total: 0 };
        this.montoEsperado = this.caja?.montoApertura || 0;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'warning',
          title: 'Advertencia',
          text: 'No se pudieron cargar las ventas. Los totales pueden estar incompletos.',
          confirmButtonColor: '#2563eb',
          customClass: { popup: 'swal-farmaceutico' }
        });
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
    if (this.modalAperturaAbierto) return;
    this.formApertura.reset({ montoApertura: '' });
    this.errorApertura = '';
    this.modalAperturaAbierto = true;
    this.cdr.detectChanges();
  }

  cerrarModalApertura(): void {
    if (this.enviandoApertura) return;
    this.modalAperturaAbierto = false;
    this.enviandoApertura = false;
    this.cdr.detectChanges();
  }

  abrirModalCierre(): void {
    if (this.modalCierreAbierto) return;
    this.formCierre.reset({ montoCierreDeclarado: '' });
    this.errorCierre = '';
    this.modalCierreAbierto = true;
    this.cdr.detectChanges();
  }

  cerrarModalCierre(): void {
    if (this.enviandoCierre) return;
    this.modalCierreAbierto = false;
    this.enviandoCierre = false;
    this.cdr.detectChanges();
  }

  // ======== APERTURA ========
  abrirCaja(): void {
    if (this.formApertura.invalid || this.enviandoApertura) return;

    this.enviandoApertura = true;
    this.errorApertura = '';
    this.cdr.detectChanges();

    const request = {
      usuarioAperturaId: this.usuarioId,
      montoApertura: this.formApertura.value.montoApertura
    };

    this.cajaService.abrir(request).subscribe({
      next: () => {
        this.enviandoApertura = false;
        this.cerrarModalApertura();
        this.cargarEstado();
        Swal.fire({
          icon: 'success',
          title: '✅ Caja abierta',
          text: 'La caja ha sido abierta exitosamente.',
          timer: 2000,
          showConfirmButton: false,
          customClass: { popup: 'swal-farmaceutico' }
        });
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.enviandoApertura = false;
        this.errorApertura = err.error?.mensaje || 'Error al abrir la caja';
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: this.errorApertura,
          confirmButtonColor: '#2563eb',
          customClass: { popup: 'swal-farmaceutico' }
        });
        console.error('Error:', err);
      }
    });
  }

  // ======== CIERRE ========
  cerrarCaja(): void {
    if (this.formCierre.invalid || this.enviandoCierre || !this.caja) return;

    this.enviandoCierre = true;
    this.errorCierre = '';
    this.cdr.detectChanges();

    const request = {
      usuarioCierreId: this.usuarioId,
      montoCierreDeclarado: this.formCierre.value.montoCierreDeclarado
    };

    this.cajaService.cerrar(request).subscribe({
      next: (response: any) => {
        this.enviandoCierre = false;
        this.cerrarModalCierre();
        this.cargarEstado();
        Swal.fire({
          icon: 'success',
          title: '✅ Caja cerrada',
          html: `
            <div style="text-align: left;">
              <p><strong>Total ventas:</strong> S/ ${response.totalVentas?.toFixed(2) || '0.00'}</p>
              <p><strong>Monto declarado:</strong> S/ ${response.montoDeclarado?.toFixed(2) || '0.00'}</p>
              <p><strong>Diferencia:</strong> S/ ${response.diferencia?.toFixed(2) || '0.00'}</p>
            </div>
          `,
          confirmButtonColor: '#2563eb',
          customClass: { popup: 'swal-farmaceutico' }
        });
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.enviandoCierre = false;
        this.errorCierre = err.error?.mensaje || 'Error al cerrar la caja';
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: this.errorCierre,
          confirmButtonColor: '#2563eb',
          customClass: { popup: 'swal-farmaceutico' }
        });
        console.error('Error:', err);
      }
    });
  }

  // ======== FORMATO ========
  formatearMonto(valor: number): string {
    return 'S/ ' + (valor || 0).toFixed(2);
  }
}