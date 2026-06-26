import { Routes } from '@angular/router';
import { LayoutComponent } from './core/layout/layout';
import { LoginComponent } from './features/auth/login/login';
import { DashboardComponent } from './features/dashboard/dashboard/dashboard';
import { AuthGuard } from './core/auth-guard'; // <--- CORREGIDO: antes era './core/auth'

export const routes: Routes = [
  // ================================================================
  // RUTA PÚBLICA (LOGIN)
  // ================================================================
  { path: 'login', component: LoginComponent },

  // ================================================================
  // RUTA PRINCIPAL (PROTEGIDA POR AUTHGUARD)
  // ================================================================
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      // Dashboard (carga directa, sin lazy loading)
      { path: 'dashboard', component: DashboardComponent },

      // ============================================================
      // CATÁLOGOS MAESTROS (Fase 2)
      // ============================================================
      {
        path: 'categorias',
        loadChildren: () => import('./features/categorias/categorias-module').then(m => m.CategoriasModule)
      },
      {
        path: 'fabricantes',
        loadChildren: () => import('./features/fabricantes/fabricantes-module').then(m => m.FabricantesModule)
      },
      {
        path: 'proveedores',
        loadChildren: () => import('./features/proveedores/proveedores-module').then(m => m.ProveedoresModule)
      },
      {
        path: 'clientes',
        loadChildren: () => import('./features/clientes/clientes-module').then(m => m.ClientesModule)
      },
      {
        path: 'productos',
        loadChildren: () => import('./features/productos/productos-module').then(m => m.ProductosModule)
      },

      // ============================================================
      // INVENTARIO Y LOTES (Fase 3)
      // ============================================================
      {
        path: 'lotes',
        loadChildren: () => import('./features/lotes/lotes-module').then(m => m.LotesModule)
      },
      {
        path: 'movimientos-stock',
        loadChildren: () => import('./features/movimientos-stock/movimientos-stock-module').then(m => m.MovimientosStockModule)
      },

      // ============================================================
      // VENTAS, CAJA Y TRANSACCIONES (Fase 4)
      // ============================================================
      
      {
        path: 'ventas',
        loadChildren: () => import('./features/ventas/ventas-module').then(m => m.VentasModule)
      },
      {
        path: 'demanda',
        loadChildren: () => import('./features/demanda/demanda-module').then(m => m.DemandaModule)
      },
      {
        path: 'bitacora',
        loadChildren: () => import('./features/bitacora/bitacora-module').then(m => m.BitacoraModule)
      },

      // ============================================================
      // REPORTES, ESTADÍSTICAS Y AUDITORÍA (Fase 5)
      // ============================================================
      {
        path: 'reportes',
        loadChildren: () => import('./features/reportes/reportes-module').then(m => m.ReportesModule)
      },
      {
        path: 'estadisticas',
        loadChildren: () => import('./features/estadisticas/estadisticas-module').then(m => m.EstadisticasModule)
      },
      {
        path: 'auditoria',
        loadChildren: () => import('./features/auditoria/auditoria-module').then(m => m.AuditoriaModule)
      },

      // ============================================================
      // REDIRECCIÓN POR DEFECTO
      // ============================================================
      { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
      {
  path: 'caja',
  loadComponent: () => import('./features/caja/listar/listar')
    .then(m => m.ListarComponent)
},
    ]
  },

  // ================================================================
  // RUTA COMODÍN (404)
  // ================================================================
  { path: '**', redirectTo: '/dashboard' }
];