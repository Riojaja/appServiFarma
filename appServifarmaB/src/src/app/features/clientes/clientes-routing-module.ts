import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListarComponent } from './listar/listar';
import { CrearComponent } from './crear/crear';
import { EditarComponent } from './editar/editar';

const routes: Routes = [
  { path: '', component: ListarComponent },
  { path: 'crear', component: CrearComponent },
  { path: 'editar/:id', component: EditarComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ClientesRoutingModule { }