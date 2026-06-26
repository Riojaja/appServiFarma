import { Component, OnInit, AfterViewInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DemandaService } from '../../../core/services/demanda';
import { AuthService } from '../../../core/auth';
import { DemandaInsatisfecha } from '../../../core/models/demanda-insatisfecha.model';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

// Registrar todos los componentes de Chart.js
Chart.register(...registerables);

@Component({
  selector: 'app-listar-demandas',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule
    // ✅ No se necesita NgChartsModule ni provideCharts
  ],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;
  private chartInstance: Chart | null = null;

  demandas: DemandaInsatisfecha[] = [];
  demandasFiltradas: DemandaInsatisfecha[] = [];
  filtroTexto: string = '';
  cargando: boolean = false;

  offcanvasAbierto: boolean = false;
  formRegistro: FormGroup;
  enviando: boolean = false;
  errorRegistro: string = '';
  usuarioId: number = 0;

  topProductos: { nombre: string, cantidad: number }[] = [];

  constructor(
    private fb: FormBuilder,
    private demandaService: DemandaService,
    private authService: AuthService
  ) {
    this.usuarioId = Number(this.authService.getUsuarioId()) || 0;
    this.formRegistro = this.fb.group({
      productoSolicitado: ['', [Validators.required, Validators.maxLength(200)]],
      clienteDocumento: ['', [Validators.maxLength(20)]]
    });
  }

  ngOnInit(): void {
    this.cargarDemandas();
  }

  ngAfterViewInit(): void {
    // Después de que la vista se renderice, creamos el gráfico
    this.crearGrafico();
  }

  ngOnDestroy(): void {
    // Destruir el gráfico al salir del componente
    if (this.chartInstance) {
      this.chartInstance.destroy();
      this.chartInstance = null;
    }
  }

  cargarDemandas(): void {
    this.cargando = true;
    this.demandaService.listar().subscribe({
      next: (data: DemandaInsatisfecha[]) => {
        this.demandas = data;
        this.aplicarFiltro();
        this.calcularTopProductos();
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar demandas:', err);
        this.cargando = false;
      }
    });
  }

  aplicarFiltro(): void {
    if (!this.filtroTexto.trim()) {
      this.demandasFiltradas = this.demandas;
      return;
    }
    const texto = this.filtroTexto.toLowerCase().trim();
    this.demandasFiltradas = this.demandas.filter(d =>
      d.productoSolicitado.toLowerCase().includes(texto) ||
      (d.clienteDocumento && d.clienteDocumento.includes(texto))
    );
  }

  limpiarFiltro(): void {
    this.filtroTexto = '';
    this.aplicarFiltro();
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar esta demanda?')) {
      this.demandaService.eliminar(id).subscribe({
        next: () => {
          this.demandas = this.demandas.filter(d => d.id !== id);
          this.aplicarFiltro();
          this.calcularTopProductos();
        },
        error: (err: any) => console.error('Error al eliminar:', err)
      });
    }
  }

  abrirOffcanvas(): void {
    this.formRegistro.reset({ productoSolicitado: '', clienteDocumento: '' });
    this.errorRegistro = '';
    this.offcanvasAbierto = true;
  }

  cerrarOffcanvas(): void {
    this.offcanvasAbierto = false;
    this.enviando = false;
  }

  registrarDemanda(): void {
    if (this.formRegistro.invalid) {
      this.errorRegistro = 'El nombre del producto es obligatorio.';
      return;
    }

    this.enviando = true;
    this.errorRegistro = '';

    const request = {
      productoSolicitado: this.formRegistro.value.productoSolicitado,
      clienteDocumento: this.formRegistro.value.clienteDocumento || undefined,
      usuarioId: this.usuarioId
    };

    this.demandaService.registrar(request).subscribe({
      next: () => {
        this.enviando = false;
        this.cerrarOffcanvas();
        this.cargarDemandas();
        alert('✅ Demanda registrada exitosamente.');
      },
      error: (err: any) => {
        this.enviando = false;
        this.errorRegistro = err.error?.mensaje || 'Error al registrar la demanda';
        console.error('Error:', err);
      }
    });
  }

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

    // Actualizar el gráfico
    this.actualizarGrafico();
  }

  private crearGrafico(): void {
    if (!this.chartCanvas) return;

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    // Destruir gráfico anterior si existe
    if (this.chartInstance) {
      this.chartInstance.destroy();
      this.chartInstance = null;
    }

    // Si no hay datos, no crear gráfico
    if (this.topProductos.length === 0) {
      return;
    }

    this.chartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: this.topProductos.map(p => p.nombre),
        datasets: [{
          label: 'Veces Solicitado',
          data: this.topProductos.map(p => p.cantidad),
          backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b'],
          borderRadius: 8,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          title: { display: false }
        },
        scales: {
          y: { beginAtZero: true }
        }
      }
    });
  }

  private actualizarGrafico(): void {
    // Si el gráfico ya existe, actualizar datos
    if (this.chartInstance) {
      this.chartInstance.data.labels = this.topProductos.map(p => p.nombre);
      this.chartInstance.data.datasets[0].data = this.topProductos.map(p => p.cantidad);
      this.chartInstance.update();
    } else {
      // Si no existe, crearlo
      this.crearGrafico();
    }
  }
}