import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProveedorService } from '../../../core/services/proveedor';
import { Proveedor } from '../../../core/models/proveedor.model';

@Component({
  selector: 'app-listar-proveedores',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  proveedores: Proveedor[] = [];
  filtroRazonSocial: string = '';
  cargando: boolean = false;

  constructor(private proveedorService: ProveedorService) { }

  ngOnInit(): void {
    this.cargarProveedores();
  }

  cargarProveedores(): void {
    this.cargando = true;
    this.proveedorService.listar().subscribe({
      next: (data: Proveedor[]) => {
        this.proveedores = data;
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar proveedores:', err);
        this.cargando = false;
      }
    });
  }

  buscar(): void {
    if (this.filtroRazonSocial.trim()) {
      this.proveedorService.buscarPorRazonSocial(this.filtroRazonSocial).subscribe({
        next: (data: Proveedor[]) => this.proveedores = data,
        error: (err: any) => console.error('Error al buscar:', err)
      });
    } else {
      this.cargarProveedores();
    }
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este proveedor?')) {
      this.proveedorService.eliminar(id).subscribe({
        next: () => {
          this.proveedores = this.proveedores.filter(p => p.id !== id);
        },
        error: (err: any) => console.error('Error al eliminar:', err)
      });
    }
  }
}