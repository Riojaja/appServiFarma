import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CajaService } from '../../../core/services/caja';
import { Caja } from '../../../core/models/caja.model';

@Component({
  selector: 'app-estado-caja',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './estado.html',
  styleUrls: ['./estado.css']
})
export class EstadoComponent implements OnInit {
  caja: Caja | null = null;
  existeCaja: boolean = false;
  cargando: boolean = false;
  errorMessage: string = '';

  constructor(private cajaService: CajaService) { }

  ngOnInit(): void {
    this.cargarEstado();
  }

  cargarEstado(): void {
    this.cargando = true;
    this.cajaService.obtenerCajaAbierta().subscribe({
      next: (data) => {
        this.caja = data;
        this.existeCaja = true;
        this.cargando = false;
      },
      error: (err) => {
        if (err.status === 404) {
          this.existeCaja = false;
          this.caja = null;
        } else {
          this.errorMessage = 'Error al obtener el estado de la caja.';
        }
        this.cargando = false;
        console.error('Error:', err);
      }
    });
  }
}