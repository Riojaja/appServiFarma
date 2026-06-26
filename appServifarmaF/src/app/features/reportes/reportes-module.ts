import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ReportesRoutingModule } from './reportes-routing-module';

import { DigemitComponent } from './digemit/digemit';
import { RentabilidadComponent } from './rentabilidad/rentabilidad';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ReportesRoutingModule,
    DigemitComponent,
    RentabilidadComponent
  ]
})
export class ReportesModule { }