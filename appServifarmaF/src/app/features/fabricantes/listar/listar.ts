import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FabricanteService } from '../../../core/services/fabricante';
import { Fabricante } from '../../../core/models/fabricante.model';
import { AuthService } from '../../../core/auth';

@Component({
  selector: 'app-listar-fabricantes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  fabricantes: Fabricante[] = [];
  filtroNombre: string = '';
  cargando: boolean = false;

  constructor(
    private fabricanteService: FabricanteService,
    public auth: AuthService
  ) { }

  ngOnInit(): void {
    this.cargarFabricantes();
  }

  cargarFabricantes(): void {
    this.cargando = true;
    this.fabricanteService.listar().subscribe({
      next: (data: Fabricante[]) => {
        this.fabricantes = data;
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar fabricantes:', err);
        this.cargando = false;
      }
    });
  }

  buscar(): void {
    if (this.filtroNombre.trim()) {
      this.fabricanteService.buscarPorNombre(this.filtroNombre).subscribe({
        next: (data: Fabricante[]) => this.fabricantes = data,
        error: (err: any) => console.error('Error al buscar:', err)
      });
    } else {
      this.cargarFabricantes();
    }
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este fabricante?')) {
      this.fabricanteService.eliminar(id).subscribe({
        next: () => {
          this.fabricantes = this.fabricantes.filter(f => f.id !== id);
        },
        error: (err: any) => console.error('Error al eliminar:', err)
      });
    }
  }
}