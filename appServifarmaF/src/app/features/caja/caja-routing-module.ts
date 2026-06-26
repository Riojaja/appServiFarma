import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AperturaComponent } from './apertura/apertura';
import { CierreComponent } from './cierre/cierre';
import { EstadoComponent } from './estado/estado';

const routes: Routes = [
  { path: 'apertura', component: AperturaComponent },
  { path: 'cierre', component: CierreComponent },
  { path: 'estado', component: EstadoComponent },
  { path: '', redirectTo: 'estado', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CajaRoutingModule { }