import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { VentasRoutingModule } from './ventas-routing-module';

import { RegistrarComponent } from './registrar/registrar';
import { ListarComponent } from './listar/listar';
import { AnularComponent } from './anular/anular';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    VentasRoutingModule,
    RegistrarComponent,
    ListarComponent,
    AnularComponent
  ]
})
export class VentasModule { }