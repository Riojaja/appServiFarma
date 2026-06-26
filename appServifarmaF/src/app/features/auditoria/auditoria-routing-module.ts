import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MovimientosStockComponent } from './movimientos-stock/movimientos-stock';
import { VentasAnuladasComponent } from './ventas-anuladas/ventas-anuladas';
import { CajasDiferenciaComponent } from './cajas-diferencia/cajas-diferencia';
import { ResumenActividadComponent } from './resumen-actividad/resumen-actividad';

const routes: Routes = [
  { path: 'movimientos-stock', component: MovimientosStockComponent },
  { path: 'ventas-anuladas', component: VentasAnuladasComponent },
  { path: 'cajas-diferencia', component: CajasDiferenciaComponent },
  { path: 'resumen-actividad', component: ResumenActividadComponent },
  { path: '', redirectTo: 'resumen-actividad', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuditoriaRoutingModule { }