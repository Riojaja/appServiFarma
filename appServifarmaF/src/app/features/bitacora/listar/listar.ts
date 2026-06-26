import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BitacoraService } from '../../../core/services/bitacora';
import { BitacoraComunicacion } from '../../../core/models/bitacora-comunicacion.model';

@Component({
  selector: 'app-listar-bitacora',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  mensajes: BitacoraComunicacion[] = [];
  cargando: boolean = false;

  constructor(private bitacoraService: BitacoraService) { }

  ngOnInit(): void {
    this.cargarMensajes();
  }

  cargarMensajes(): void {
    this.cargando = true;
    this.bitacoraService.listar().subscribe({
      next: (data) => {
        this.mensajes = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error:', err);
        this.cargando = false;
      }
    });
  }

  marcarLeido(id: number): void {
    this.bitacoraService.marcarLeido(id).subscribe({
      next: () => {
        const mensaje = this.mensajes.find(m => m.id === id);
        if (mensaje) mensaje.leido = true;
      },
      error: (err) => console.error('Error al marcar como leído:', err)
    });
  }

  eliminar(id: number): void {
    if (confirm('¿Eliminar este mensaje?')) {
      this.bitacoraService.eliminar(id).subscribe({
        next: () => {
          this.mensajes = this.mensajes.filter(m => m.id !== id);
        },
        error: (err) => console.error('Error:', err)
      });
    }
  }
}