import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DemandaRoutingModule } from './demanda-routing-module';

import { RegistrarComponent } from './registrar/registrar';
import { ListarComponent } from './listar/listar';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    DemandaRoutingModule,
    RegistrarComponent,
    ListarComponent
  ]
})
export class DemandaModule { }