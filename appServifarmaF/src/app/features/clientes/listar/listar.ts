import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../../core/services/cliente';
import { Cliente } from '../../../core/models/cliente.model';

@Component({
  selector: 'app-listar-clientes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  clientes: Cliente[] = [];
  clientesFiltrados: Cliente[] = [];
  filtroTexto: string = '';
  cargando: boolean = false;

  constructor(private clienteService: ClienteService) { }

  ngOnInit(): void {
    this.cargarClientes();
  }

  cargarClientes(): void {
    this.cargando = true;
    this.clienteService.listar().subscribe({
      next: (data: Cliente[]) => {
        this.clientes = data;
        this.aplicarFiltro();
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar clientes:', err);
        this.cargando = false;
      }
    });
  }

  aplicarFiltro(): void {
    if (!this.filtroTexto.trim()) {
      this.clientesFiltrados = this.clientes;
      return;
    }

    const texto = this.filtroTexto.toLowerCase().trim();
    this.clientesFiltrados = this.clientes.filter(c => 
      c.nombre.toLowerCase().includes(texto) ||
      c.documentoNumero.includes(texto) ||
      c.documentoTipo.toLowerCase().includes(texto) ||
      (c.telefono && c.telefono.includes(texto)) ||
      (c.email && c.email.toLowerCase().includes(texto))
    );
  }

  limpiarFiltro(): void {
    this.filtroTexto = '';
    this.aplicarFiltro();
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este cliente? Esta acción no se puede deshacer.')) {
      this.clienteService.eliminar(id).subscribe({
        next: () => {
          this.clientes = this.clientes.filter(c => c.id !== id);
          this.aplicarFiltro();
        },
        error: (err: any) => {
          console.error('Error al eliminar cliente:', err);
          alert(err.error?.mensaje || 'Error al eliminar el cliente.');
        }
      });
    }
  }
}