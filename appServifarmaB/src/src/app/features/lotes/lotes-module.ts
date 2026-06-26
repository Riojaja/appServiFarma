import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LotesRoutingModule } from './lotes-routing-module';

import { ListarComponent } from './listar/listar';
import { CrearComponent } from './crear/crear';
import { EditarComponent } from './editar/editar';
import { AjustarStockComponent } from './ajustar-stock/ajustar-stock';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    LotesRoutingModule,
    ListarComponent,
    CrearComponent,
    EditarComponent,
    AjustarStockComponent
  ]
})
export class LotesModule { }