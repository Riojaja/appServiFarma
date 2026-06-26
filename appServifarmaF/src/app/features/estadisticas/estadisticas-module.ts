import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { EstadisticasRoutingModule } from './estadisticas-routing-module';
import { DashboardComponent } from './dashboard/dashboard';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    EstadisticasRoutingModule,
    DashboardComponent  // Es standalone
  ]
})
export class EstadisticasModule { }