import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CajaRoutingModule } from './caja-routing-module';

import { AperturaComponent } from './apertura/apertura';
import { CierreComponent } from './cierre/cierre';
import { EstadoComponent } from './estado/estado';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    CajaRoutingModule,
    AperturaComponent,
    CierreComponent,
    EstadoComponent
  ]
})
export class CajaModule { }