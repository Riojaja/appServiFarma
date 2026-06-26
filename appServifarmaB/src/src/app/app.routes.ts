import { Routes } from '@angular/router';
import { LayoutComponent } from './core/layout/layout';
import { LoginComponent } from './features/auth/login/login';
import { DashboardComponent } from './features/dashboard/dashboard/dashboard';
import { AuthGuard } from './core/auth-guard';

export const routes: Routes = [
  // Ruta pública (login)
  { path: 'login', component: LoginComponent },

  // Ruta principal (protegida por AuthGuard)
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      // Dashboard
      { path: 'dashboard', component: DashboardComponent },

      // Inventario (alias de productos/listar)
      {
        path: 'inventario',
        loadChildren: () => import('./features/productos/productos-module').then(m => m.ProductosModule)
      },

      // Buscador Visual


      // Catálogos maestros
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

      // Lotes y movimientos
      {
        path: 'lotes',
        loadChildren: () => import('./features/lotes/lotes-module').then(m => m.LotesModule)
      },
      {
        path: 'movimientos-stock',
        loadChildren: () => import('./features/movimientos-stock/movimientos-stock-module').then(m => m.MovimientosStockModule)
      },

      // Caja y ventas
      {
        path: 'caja',
        loadChildren: () => import('./features/caja/caja-module').then(m => m.CajaModule)
      },
      {
        path: 'ventas',
        loadChildren: () => import('./features/ventas/ventas-module').then(m => m.VentasModule)
      },

      // Demanda y bitácora
      {
        path: 'demanda',
        loadChildren: () => import('./features/demanda/demanda-module').then(m => m.DemandaModule)
      },
      {
        path: 'bitacora',
        loadChildren: () => import('./features/bitacora/bitacora-module').then(m => m.BitacoraModule)
      },

      // Reportes y estadísticas
      {
        path: 'reportes',
        loadChildren: () => import('./features/reportes/reportes-module').then(m => m.ReportesModule)
      },
      {
        path: 'estadisticas',
        loadChildren: () => import('./features/estadisticas/estadisticas-module').then(m => m.EstadisticasModule)
      },

      // Auditoría
      {
        path: 'auditoria',
        loadChildren: () => import('./features/auditoria/auditoria-module').then(m => m.AuditoriaModule)
      },

      // Redirección por defecto
      { path: '', redirectTo: '/dashboard', pathMatch: 'full' },

      {
        path: 'productos',
        loadChildren: () => import('./features/productos/productos-module').then(m => m.ProductosModule)
      }
    ]
  },

  // Ruta comodín
  { path: '**', redirectTo: '/dashboard' }
];