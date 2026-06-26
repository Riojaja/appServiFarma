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
  filtroNombre: string = '';
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
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar clientes:', err);
        this.cargando = false;
      }
    });
  }

  buscar(): void {
    if (this.filtroNombre.trim()) {
      this.clienteService.buscarPorNombre(this.filtroNombre).subscribe({
        next: (data: Cliente[]) => this.clientes = data,
        error: (err: any) => console.error('Error al buscar:', err)
      });
    } else {
      this.cargarClientes();
    }
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este cliente?')) {
      this.clienteService.eliminar(id).subscribe({
        next: () => {
          this.clientes = this.clientes.filter(c => c.id !== id);
        },
        error: (err: any) => console.error('Error al eliminar:', err)
      });
    }
  }
}