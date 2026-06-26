import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DigemitComponent } from './digemit/digemit';
import { RentabilidadComponent } from './rentabilidad/rentabilidad';

const routes: Routes = [
  { path: 'digemit', component: DigemitComponent },
  { path: 'rentabilidad', component: RentabilidadComponent },
  { path: '', redirectTo: 'digemit', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportesRoutingModule { }