import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../../../core/services/usuario';
import { Usuario } from '../../../core/models/usuario.model';

@Component({
  selector: 'app-listar-usuarios',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  usuarios: Usuario[] = [];
  usuariosFiltrados: Usuario[] = [];
  filtroTexto: string = '';
  cargando: boolean = false;

  constructor(private usuarioService: UsuarioService) { }

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.cargando = true;
    this.usuarioService.listar().subscribe({
      next: (data: Usuario[]) => {
        this.usuarios = data;
        this.aplicarFiltro();
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error al cargar usuarios:', err);
        this.cargando = false;
      }
    });
  }

  aplicarFiltro(): void {
    if (!this.filtroTexto.trim()) {
      this.usuariosFiltrados = this.usuarios;
      return;
    }
    const texto = this.filtroTexto.toLowerCase().trim();
    this.usuariosFiltrados = this.usuarios.filter(u =>
      u.nombreCompleto.toLowerCase().includes(texto) ||
      u.usuario.toLowerCase().includes(texto) ||
      (u.rol && u.rol.toLowerCase().includes(texto))
    );
  }

  limpiarFiltro(): void {
    this.filtroTexto = '';
    this.aplicarFiltro();
  }

  getRolBadge(rol: string): string {
    const map: any = {
      'admin': 'bg-danger',
      'vendedor': 'bg-primary'
    };
    return map[rol?.toLowerCase()] || 'bg-secondary';
  }
}