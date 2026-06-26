import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegistrarComponent } from './registrar/registrar';
import { ListarComponent } from './listar/listar';
import { AnularComponent } from './anular/anular';

const routes: Routes = [
  { path: 'registrar', component: RegistrarComponent },
  { path: 'listar', component: ListarComponent },
  { path: 'anular/:id', component: AnularComponent },
  { path: '', redirectTo: 'listar', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class VentasRoutingModule { }