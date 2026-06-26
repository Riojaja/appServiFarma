import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProductosRoutingModule } from './productos-routing-module';

import { ListarComponent } from './listar/listar';
import { CrearComponent } from './crear/crear';
import { EditarComponent } from './editar/editar';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ProductosRoutingModule,
    ListarComponent,
    CrearComponent,
    EditarComponent
  ]
})
export class ProductosModule { }