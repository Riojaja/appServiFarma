import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CrearComponent } from './crear/crear';
import { ListarComponent } from './listar/listar';

const routes: Routes = [
  { path: 'crear', component: CrearComponent },
  { path: 'listar', component: ListarComponent },
  { path: '', redirectTo: 'listar', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BitacoraRoutingModule { }