import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardRoutingModule } from './dashboard-routing-module';
import { DashboardComponent } from './dashboard/dashboard';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    DashboardRoutingModule,
    DashboardComponent // Es standalone, se importa, no se declara
  ]
})
export class DashboardModule { }