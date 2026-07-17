import { Component, OnInit, AfterViewInit, OnDestroy, ElementRef, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DemandaService } from '../../../core/services/demanda';
import { AuthService } from '../../../core/auth';
import { DemandaInsatisfecha } from '../../../core/models/demanda-insatisfecha.model';
import { Chart, registerables } from 'chart.js';
import Swal from 'sweetalert2';
import { finalize } from 'rxjs/operators';

Chart.register(...registerables);

@Component({
  selector: 'app-listar-demandas',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('chartCanvas', { static: false }) chartCanvas!: ElementRef<HTMLCanvasElement>;
  private chartInstance: Chart | null = null;
  private intentosGrafico: number = 0;

  demandas: DemandaInsatisfecha[] = [];
  demandasFiltradas: DemandaInsatisfecha[] = [];
  filtroTexto: string = '';
  cargando: boolean = false;
  datosCargados: boolean = false;

  // Offcanvas (Nuevo registro)
  offcanvasAbierto: boolean = false;
  formRegistro: FormGroup;
  enviando: boolean = false;
  errorRegistro: string = '';
  usuarioId: number = 0;

  // Top productos
  topProductos: { nombre: string, cantidad: number }[] = [];

  constructor(
    private fb: FormBuilder,
    private demandaService: DemandaService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
    this.formRegistro = this.fb.group({
      productoSolicitado: ['', [Validators.required, Validators.maxLength(200)]],
      clienteDocumento: ['', [Validators.maxLength(20)]]
    });
  }

  ngOnInit(): void {
    // ✅ Cargar datos al instante (sin esperar a que el DOM esté listo)
    this.cargarDemandas();
  }

  ngAfterViewInit(): void {
    // Si los datos ya están cargados, crear gráfico
    if (this.datosCargados && this.topProductos.length > 0) {
      this.crearGrafico();
    } else {
      // Si no, esperar un poco y reintentar
      setTimeout(() => {
        if (this.datosCargados && this.topProductos.length > 0) {
          this.crearGrafico();
        }
      }, 500);
    }
  }

  ngOnDestroy(): void {
    if (this.chartInstance) {
      this.chartInstance.destroy();
      this.chartInstance = null;
    }
  }

  // ============================================================
  // CARGA DE DATOS (con finalize para asegurar que el spinner se oculta)
  // ============================================================
  cargarDemandas(): void {
    if (this.cargando) return; // Evita múltiples llamadas

    this.cargando = true;
    this.datosCargados = false;
    this.intentosGrafico = 0;

    console.log('🔄 Cargando demandas...');

    this.demandaService.listar()
      .pipe(
        finalize(() => {
          // ⚠️ Esto se ejecuta SIEMPRE (éxito o error)
          this.cargando = false;
          this.datosCargados = true;
          console.log('🔄 Carga finalizada. cargando=false');
          // Forzar detección de cambios para actualizar la vista
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (data: DemandaInsatisfecha[]) => {
          this.demandas = data || [];
          console.log('✅ Demandas recibidas:', this.demandas.length);
          // Aplicar filtros y calcular top productos
          this.aplicarFiltro();
          this.calcularTopProductos();
          // Si el gráfico aún no se creó, intentarlo (por si el canvas ya está listo)
          if (!this.chartInstance && this.topProductos.length > 0) {
            this.crearGrafico();
          }
          // Forzar detección de cambios para actualizar la tabla
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          console.error('❌ Error al cargar demandas:', err);
          this.demandas = [];
          this.demandasFiltradas = [];
          this.topProductos = [];
          Swal.fire({
            title: 'Error',
            text: err.error?.mensaje || 'No se pudieron cargar las demandas',
            icon: 'error',
            customClass: { popup: 'swal-farmaceutico' }
          });
          this.cdr.detectChanges();
        }
      });
  }

  // ============================================================
  // FILTROS
  // ============================================================
  aplicarFiltro(): void {
    if (!this.filtroTexto.trim()) {
      this.demandasFiltradas = [...this.demandas];
      return;
    }
    const texto = this.filtroTexto.toLowerCase().trim();
    this.demandasFiltradas = this.demandas.filter(d =>
      d.productoSolicitado.toLowerCase().includes(texto) ||
      (d.clienteDocumento && d.clienteDocumento.includes(texto))
    );
    this.cdr.detectChanges();
  }

  limpiarFiltro(): void {
    this.filtroTexto = '';
    this.aplicarFiltro();
  }

  // ============================================================
  // ELIMINAR (con confirmación y prevención de doble clic)
  // ============================================================
  eliminar(id: number): void {
    if (this.enviando) return; // 🔒 Evita doble clic

    Swal.fire({
      title: '¿Eliminar demanda?',
      text: 'Esta acción no se puede deshacer.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#6b7280',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      customClass: { popup: 'swal-farmaceutico' }
    }).then((result) => {
      if (result.isConfirmed) {
        this.enviando = true;
        this.demandaService.eliminar(id)
          .pipe(finalize(() => this.enviando = false))
          .subscribe({
            next: () => {
              // Eliminar localmente sin recargar toda la lista
              this.demandas = this.demandas.filter(d => d.id !== id);
              this.aplicarFiltro();
              this.calcularTopProductos(); // Actualiza gráfico
              Swal.fire({
                title: 'Eliminado',
                text: 'Demanda eliminada correctamente',
                icon: 'success',
                timer: 1500,
                showConfirmButton: false,
                customClass: { popup: 'swal-farmaceutico' }
              });
              this.cdr.detectChanges();
            },
            error: (err) => {
              console.error('Error al eliminar:', err);
              Swal.fire({
                title: 'Error',
                text: err.error?.mensaje || 'No se pudo eliminar la demanda',
                icon: 'error',
                customClass: { popup: 'swal-farmaceutico' }
              });
              this.cdr.detectChanges();
            }
          });
      }
    });
  }

  // ============================================================
  // OFFCANVAS (NUEVA DEMANDA) - con prevención de doble clic
  // ============================================================
  abrirOffcanvas(): void {
    if (this.offcanvasAbierto) return; // 🔒 Evita doble apertura
    this.formRegistro.reset({ productoSolicitado: '', clienteDocumento: '' });
    this.errorRegistro = '';
    this.offcanvasAbierto = true;
    this.cdr.detectChanges();
  }

  cerrarOffcanvas(): void {
    if (this.enviando) return; // No cerrar mientras se está enviando
    this.offcanvasAbierto = false;
    this.enviando = false;
    this.cdr.detectChanges();
  }

  registrarDemanda(): void {
    if (this.enviando) return; // 🔒 Evita doble clic

    if (this.formRegistro.invalid) {
      this.errorRegistro = 'El nombre del producto es obligatorio.';
      Swal.fire({
        title: 'Formulario incompleto',
        text: 'El nombre del producto es obligatorio.',
        icon: 'warning',
        customClass: { popup: 'swal-farmaceutico' }
      });
      return;
    }

    this.enviando = true;
    this.errorRegistro = '';

    const request = {
      productoSolicitado: this.formRegistro.value.productoSolicitado,
      clienteDocumento: this.formRegistro.value.clienteDocumento || undefined,
      usuarioId: this.usuarioId
    };

    this.demandaService.registrar(request)
      .pipe(finalize(() => this.enviando = false))
      .subscribe({
        next: () => {
          this.cerrarOffcanvas();
          // Recargar datos (esto actualiza lista y gráfico)
          this.cargarDemandas();
          Swal.fire({
            title: '✅ Demanda registrada',
            text: 'La demanda fue registrada exitosamente.',
            icon: 'success',
            timer: 1500,
            showConfirmButton: false,
            customClass: { popup: 'swal-farmaceutico' }
          });
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorRegistro = err.error?.mensaje || 'Error al registrar la demanda';
          console.error('Error:', err);
          Swal.fire({
            title: 'Error',
            text: this.errorRegistro,
            icon: 'error',
            customClass: { popup: 'swal-farmaceutico' }
          });
          this.cdr.detectChanges();
        }
      });
  }

  // ============================================================
  // TOP PRODUCTOS (cálculo y actualización del gráfico)
  // ============================================================
  calcularTopProductos(): void {
    const conteo: { [key: string]: number } = {};
    this.demandas.forEach(d => {
      const nombre = d.productoSolicitado;
      conteo[nombre] = (conteo[nombre] || 0) + 1;
    });

    const sorted = Object.entries(conteo)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5);

    this.topProductos = sorted.map(([nombre, cantidad]) => ({ nombre, cantidad }));
    console.log('📊 Top productos calculado:', this.topProductos);

    // Actualizar gráfico (crear o actualizar)
    this.actualizarGrafico();
    this.cdr.detectChanges();
  }

  // ============================================================
  // GRÁFICO (creación y actualización robusta)
  // ============================================================
  private crearGrafico(): void {
    // Si ya hay gráfico, destruirlo primero
    if (this.chartInstance) {
      this.chartInstance.destroy();
      this.chartInstance = null;
    }

    // Verificar que el canvas exista
    if (!this.chartCanvas) {
      console.warn('⏳ Canvas no disponible (intento ' + (this.intentosGrafico + 1) + ')');
      if (this.intentosGrafico < 5) {
        this.intentosGrafico++;
        setTimeout(() => this.crearGrafico(), 300);
      }
      return;
    }

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) {
      console.warn('⏳ Contexto 2D no disponible');
      if (this.intentosGrafico < 5) {
        this.intentosGrafico++;
        setTimeout(() => this.crearGrafico(), 300);
      }
      return;
    }

    if (this.topProductos.length === 0) {
      console.log('📊 Sin datos para el gráfico');
      return;
    }

    const colors = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b'];

    this.chartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: this.topProductos.map(p => p.nombre),
        datasets: [{
          label: 'Veces Solicitado',
          data: this.topProductos.map(p => p.cantidad),
          backgroundColor: colors.slice(0, this.topProductos.length),
          borderRadius: 8,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false }, title: { display: false } },
        scales: {
          y: { beginAtZero: true, ticks: { stepSize: 1 } }
        }
      }
    });
    console.log('📊 Gráfico creado exitosamente');
    this.cdr.detectChanges();
  }

  private actualizarGrafico(): void {
    if (this.chartInstance) {
      if (this.topProductos.length === 0) {
        this.chartInstance.destroy();
        this.chartInstance = null;
        console.log('📊 Gráfico destruido (sin datos)');
        return;
      }
      this.chartInstance.data.labels = this.topProductos.map(p => p.nombre);
      this.chartInstance.data.datasets[0].data = this.topProductos.map(p => p.cantidad);
      this.chartInstance.update();
      console.log('📊 Gráfico actualizado');
      this.cdr.detectChanges();
    } else {
      if (this.topProductos.length > 0) {
        this.crearGrafico();
      }
    }
  }
}