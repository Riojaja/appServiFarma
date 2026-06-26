import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DemandaService } from '../../../core/services/demanda';
import { DemandaInsatisfecha } from '../../../core/models/demanda-insatisfecha.model';

@Component({
  selector: 'app-listar-demandas',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css']
})
export class ListarComponent implements OnInit {
  demandas: DemandaInsatisfecha[] = [];
  cargando: boolean = false;

  constructor(private demandaService: DemandaService) { }

  ngOnInit(): void {
    this.cargarDemandas();
  }

  cargarDemandas(): void {
    this.cargando = true;
    this.demandaService.listar().subscribe({
      next: (data) => {
        this.demandas = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error:', err);
        this.cargando = false;
      }
    });
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar esta demanda?')) {
      this.demandaService.eliminar(id).subscribe({
        next: () => {
          this.demandas = this.demandas.filter(d => d.id !== id);
        },
        error: (err) => console.error('Error:', err)
      });
    }
  }
}