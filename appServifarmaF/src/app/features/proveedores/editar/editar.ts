import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProveedorService } from '../../../core/services/proveedor';
import { Proveedor } from '../../../core/models/proveedor.model';

@Component({
  selector: 'app-editar-proveedor',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './editar.html',
  styleUrls: ['./editar.css']
})
export class EditarComponent implements OnInit {
  form: FormGroup;
  enviando: boolean = false;
  proveedorId!: number;
  cargando: boolean = true;

  constructor(
    private fb: FormBuilder,
    private proveedorService: ProveedorService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      ruc: ['', [Validators.required, Validators.minLength(11), Validators.maxLength(11)]],
      razonSocial: ['', [Validators.required, Validators.maxLength(150)]],
      direccion: ['', [Validators.maxLength(200)]],
      telefono: ['', [Validators.maxLength(20)]],
      email: ['', [Validators.email, Validators.maxLength(100)]],
      contacto: ['', [Validators.maxLength(100)]],
      region: ['', [Validators.maxLength(50)]]
    });
  }

  ngOnInit(): void {
    this.proveedorId = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarProveedor();
  }

  cargarProveedor(): void {
    this.cargando = true;
    this.proveedorService.obtener(this.proveedorId).subscribe({
      next: (data: Proveedor) => {
        this.form.patchValue({
          ruc: data.ruc,
          razonSocial: data.razonSocial,
          direccion: data.direccion,
          telefono: data.telefono,
          email: data.email,
          contacto: data.contacto,
          region: data.region
        });
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar proveedor:', err);
        this.cargando = false;
        this.router.navigate(['/proveedores']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.proveedorService.actualizar(this.proveedorId, this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/proveedores']);
      },
      error: (err: any) => {
        console.error('Error al actualizar:', err);
        this.enviando = false;
        if (err.error?.mensaje) {
          alert(err.error.mensaje);
        }
      }
    });
  }
}