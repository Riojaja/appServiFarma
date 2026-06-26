// src/app/features/lotes/lotes-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListarComponent } from './listar/listar';
import { CrearComponent } from './crear/crear';
import { EditarComponent } from './editar/editar';

const routes: Routes = [
  { path: '', component: ListarComponent },
  { path: 'crear', component: CrearComponent },
  { path: 'editar/:id', component: EditarComponent },
  // Nueva ruta para lotes próximos a vencer (usando el mismo listar con filtro)
  { path: 'proximos-a-vencer', component: ListarComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LotesRoutingModule { }