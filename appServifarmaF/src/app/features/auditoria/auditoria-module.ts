import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuditoriaRoutingModule } from './auditoria-routing-module';

import { MovimientosStockComponent } from './movimientos-stock/movimientos-stock';
import { VentasAnuladasComponent } from './ventas-anuladas/ventas-anuladas';
import { CajasDiferenciaComponent } from './cajas-diferencia/cajas-diferencia';
import { ResumenActividadComponent } from './resumen-actividad/resumen-actividad';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    AuditoriaRoutingModule,
    MovimientosStockComponent,
    VentasAnuladasComponent,
    CajasDiferenciaComponent,
    ResumenActividadComponent
  ]
})
export class AuditoriaModule { }