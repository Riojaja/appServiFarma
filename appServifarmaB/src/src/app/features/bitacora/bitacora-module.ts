import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BitacoraRoutingModule } from './bitacora-routing-module';

import { CrearComponent } from './crear/crear';
import { ListarComponent } from './listar/listar';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    BitacoraRoutingModule,
    CrearComponent,
    ListarComponent
  ]
})
export class BitacoraModule { }