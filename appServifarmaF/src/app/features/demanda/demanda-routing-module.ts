import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegistrarComponent } from './registrar/registrar';
import { ListarComponent } from './listar/listar';

const routes: Routes = [
  { path: 'registrar', component: RegistrarComponent },
  { path: 'listar', component: ListarComponent },
  { path: '', redirectTo: 'listar', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DemandaRoutingModule { }