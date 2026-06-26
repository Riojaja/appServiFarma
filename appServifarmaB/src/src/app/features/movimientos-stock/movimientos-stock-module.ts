import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MovimientosStockRoutingModule } from './movimientos-stock-routing-module';

import { ListarComponent } from './listar/listar';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    MovimientosStockRoutingModule,
    ListarComponent
  ]
})
export class MovimientosStockModule { }