import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClienteService } from '../../../core/services/cliente';
import { Cliente } from '../../../core/models/cliente.model';

@Component({
  selector: 'app-editar-cliente',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './editar.html',
  styleUrls: ['./editar.css']
})
export class EditarComponent implements OnInit {
  form: FormGroup;
  enviando: boolean = false;
  clienteId!: number;
  cargando: boolean = true;
  tiposDocumento = ['DNI', 'RUC', 'Pasaporte'];

  constructor(
    private fb: FormBuilder,
    private clienteService: ClienteService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      documentoTipo: ['DNI', Validators.required],
      documentoNumero: ['', [Validators.required, Validators.maxLength(20)]],
      telefono: ['', [Validators.maxLength(20)]],
      direccion: ['', [Validators.maxLength(150)]],
      email: ['', [Validators.email, Validators.maxLength(100)]]
    });
  }

  ngOnInit(): void {
    this.clienteId = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarCliente();
  }

  cargarCliente(): void {
    this.cargando = true;
    this.clienteService.obtener(this.clienteId).subscribe({
      next: (data: Cliente) => {
        this.form.patchValue({
          nombre: data.nombre,
          documentoTipo: data.documentoTipo,
          documentoNumero: data.documentoNumero,
          telefono: data.telefono,
          direccion: data.direccion,
          email: data.email
        });
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar cliente:', err);
        this.cargando = false;
        this.router.navigate(['/clientes']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.enviando = true;
    this.clienteService.actualizar(this.clienteId, this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/clientes']);
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