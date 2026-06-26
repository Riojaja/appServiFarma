import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProveedorService } from '../../../core/services/proveedor';
import { Proveedor } from '../../../core/models/proveedor.model';

@Component({
  selector: 'app-listar-proveedores',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  // ======== LISTADO ========
  proveedores: Proveedor[] = [];
  proveedoresFiltrados: Proveedor[] = [];
  filtroTexto: string = '';
  cargando: boolean = false;

  // ======== OFFCANVAS CREAR ========
  offcanvasCrearAbierto: boolean = false;
  formCrear: FormGroup;
  enviandoCrear: boolean = false;
  errorCrear: string = '';

  // ======== MODAL EDITAR ========
  modalEditarAbierto: boolean = false;
  cargandoDatosEditar: boolean = false; 
  formEditar: FormGroup;
  proveedorIdEditar!: number;
  enviandoEditar: boolean = false;
  errorEditar: string = '';

  constructor(
    private fb: FormBuilder,
    private proveedorService: ProveedorService
  ) {
    this.formCrear = this.fb.group({
      ruc: ['', [Validators.required, Validators.minLength(11), Validators.maxLength(11)]],
      razonSocial: ['', [Validators.required, Validators.maxLength(150)]],
      direccion: ['', Validators.maxLength(200)],
      telefono: ['', Validators.maxLength(20)],
      email: ['', [Validators.email, Validators.maxLength(100)]],
      contacto: ['', Validators.maxLength(100)],
      region: ['', Validators.maxLength(50)]
    });

    this.formEditar = this.fb.group({
      ruc: ['', [Validators.required, Validators.minLength(11), Validators.maxLength(11)]],
      razonSocial: ['', [Validators.required, Validators.maxLength(150)]],
      direccion: ['', Validators.maxLength(200)],
      telefono: ['', Validators.maxLength(20)],
      email: ['', [Validators.email, Validators.maxLength(100)]],
      contacto: ['', Validators.maxLength(100)],
      region: ['', Validators.maxLength(50)]
    });
  }

  ngOnInit(): void {
    this.cargarProveedores();
  }

  cargarProveedores(): void {
    this.cargando = true;
    this.proveedorService.listar().subscribe({
      next: (data: Proveedor[]) => {
        this.proveedores = data;
        this.aplicarFiltro();
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar proveedores:', err);
        this.cargando = false;
      }
    });
  }

  aplicarFiltro(): void {
    if (!this.filtroTexto.trim()) {
      this.proveedoresFiltrados = this.proveedores;
      return;
    }
    const texto = this.filtroTexto.toLowerCase().trim();
    this.proveedoresFiltrados = this.proveedores.filter(p =>
      p.razonSocial.toLowerCase().includes(texto) ||
      p.ruc.includes(texto) ||
      (p.contacto && p.contacto.toLowerCase().includes(texto))
    );
  }

  limpiarFiltro(): void {
    this.filtroTexto = '';
    this.aplicarFiltro();
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este proveedor? Esta acción no se puede deshacer.')) {
      this.proveedorService.eliminar(id).subscribe({
        next: () => {
          this.proveedores = this.proveedores.filter(p => p.id !== id);
          this.aplicarFiltro();
        },
        error: (err: any) => {
          console.error('Error al eliminar:', err);
          alert(err.error?.mensaje || 'Error al eliminar el proveedor.');
        }
      });
    }
  }

  // ======== OFFCANVAS CREAR ========
  abrirOffcanvasCrear(): void {
    this.formCrear.reset({
      ruc: '',
      razonSocial: '',
      direccion: '',
      telefono: '',
      email: '',
      contacto: '',
      region: ''
    });
    this.errorCrear = '';
    this.offcanvasCrearAbierto = true;
  }

  cerrarOffcanvasCrear(): void {
    this.offcanvasCrearAbierto = false;
    this.enviandoCrear = false;
  }

  crearProveedor(): void {
    if (this.formCrear.invalid) {
      this.errorCrear = 'Por favor, complete todos los campos obligatorios correctamente.';
      return;
    }

    this.enviandoCrear = true;
    this.errorCrear = '';

    this.proveedorService.crear(this.formCrear.value).subscribe({
      next: () => {
        this.enviandoCrear = false;
        this.cerrarOffcanvasCrear();
        this.cargarProveedores();
        alert('✅ Proveedor creado exitosamente.');
      },
      error: (err: any) => {
        this.enviandoCrear = false;
        this.errorCrear = err.error?.mensaje || 'Error al crear el proveedor.';
        console.error('Error:', err);
      }
    });
  }

  // ======== MODAL EDITAR (USANDO DATOS LOCALES) ========
  abrirModalEditar(id: number): void {
    this.proveedorIdEditar = id;
    this.modalEditarAbierto = true;
    this.errorEditar = '';
    this.enviandoEditar = false;

    // ✅ Reseteamos el formulario antes de cargar
    this.formEditar.reset({
      ruc: '',
      razonSocial: '',
      direccion: '',
      telefono: '',
      email: '',
      contacto: '',
      region: ''
    });

    // ✅ Buscar el proveedor en la lista local
    const proveedor = this.proveedores.find(p => p.id === id);
    
    if (proveedor) {
      this.formEditar.patchValue({
        ruc: proveedor.ruc,
        razonSocial: proveedor.razonSocial,
        direccion: proveedor.direccion || '',
        telefono: proveedor.telefono || '',
        email: proveedor.email || '',
        contacto: proveedor.contacto || '',
        region: proveedor.region || ''
      });
    } else {
      this.errorEditar = 'No se encontraron datos del proveedor.';
      alert('Error: No se encontraron datos del proveedor.');
    }
  }

  cerrarModalEditar(): void {
    this.modalEditarAbierto = false;
    this.enviandoEditar = false;
    this.errorEditar = '';
  }

  actualizarProveedor(): void {
    if (this.formEditar.invalid) {
      this.errorEditar = 'Por favor, complete todos los campos obligatorios correctamente.';
      return;
    }

    this.enviandoEditar = true;
    this.errorEditar = '';

    this.proveedorService.actualizar(this.proveedorIdEditar, this.formEditar.value).subscribe({
      next: () => {
        this.enviandoEditar = false;
        this.cerrarModalEditar();
        this.cargarProveedores();
        alert('✅ Proveedor actualizado exitosamente.');
      },
      error: (err: any) => {
        this.enviandoEditar = false;
        this.errorEditar = err.error?.mensaje || 'Error al actualizar el proveedor.';
        console.error('Error:', err);
      }
    });
  }
} 