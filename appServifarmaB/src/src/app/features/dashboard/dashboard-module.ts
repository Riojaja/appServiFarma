import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardRoutingModule } from './dashboard-routing-module';
import { DashboardComponent } from './dashboard/dashboard';

@NgModule({
  imports: [
    CommonModule,
    DashboardRoutingModule,
    DashboardComponent      // <-- Importar en lugar de declarar
  ]
})
export class DashboardModule { }