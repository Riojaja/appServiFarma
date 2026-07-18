import { Routes } from '@angular/router';
import { LayoutComponent } from './core/layout/layout';
import { LoginComponent } from './features/auth/login/login';
import { DashboardComponent } from './features/dashboard/dashboard/dashboard';
import { AuthGuard } from './core/auth-guard';

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
      // CATÁLOGOS MAESTROS
      // ============================================================
      {
        path: 'categorias',
        children: [
          { path: '', loadComponent: () => import('./features/categorias/listar/listar').then(m => m.ListarComponent) },
        ]
      },
      {
        path: 'fabricantes',
        children: [
          { path: '', loadComponent: () => import('./features/fabricantes/listar/listar').then(m => m.ListarFabricantesComponent) },
        ]
      },
      {
        path: 'proveedores',
        canActivate: [AuthGuard],
        children: [
          {
            path: '',
            loadComponent: () => import('./features/proveedores/listar/listar')
              .then(m => m.ListarProveedoresComponent)
          }

        ]
      },
      {
        path: 'clientes',
        children: [
          { path: '', loadComponent: () => import('./features/clientes/listar/listar').then(m => m.ListarComponent) },
        ]
      },
      {
        path: 'productos',
        children: [
          { path: '', loadComponent: () => import('./features/productos/listar/listar').then(m => m.ListarComponent) },
        ]
      },

      // ============================================================
      // INVENTARIO Y LOTES
      // ============================================================
      {
        path: 'lotes',
        children: [
          { path: '', loadComponent: () => import('./features/lotes/listar/listar').then(m => m.ListarComponent) },
          // Reutiliza el mismo listar con un filtro aplicado (igual que antes en lotes-routing-module.ts)
          { path: 'proximos-a-vencer', loadComponent: () => import('./features/lotes/listar/listar').then(m => m.ListarComponent) }
        ]
      },
      {
        path: 'movimientos-stock',
        loadComponent: () => import('./features/movimientos-stock/listar/listar').then(m => m.ListarComponent)
      },

      // ============================================================
      // VENTAS, CAJA Y TRANSACCIONES
      // ============================================================
      {
        path: 'ventas',
        children: [
          { path: 'listar', loadComponent: () => import('./features/ventas/listar/listar').then(m => m.ListarComponent) },
          { path: '', redirectTo: 'listar', pathMatch: 'full' }
        ]
      },
      {
        path: 'demanda',
        children: [
          { path: 'listar', loadComponent: () => import('./features/demanda/listar/listar').then(m => m.ListarComponent) },
          { path: '', redirectTo: 'listar', pathMatch: 'full' }
        ]
      },
      {
        path: 'bitacora',
        children: [
          { path: 'listar', loadComponent: () => import('./features/bitacora/listar/listar').then(m => m.ListarComponent) },
          { path: '', redirectTo: 'listar', pathMatch: 'full' }
        ]
      },

      // ============================================================
      // REPORTES, ESTADÍSTICAS Y AUDITORÍA
      // ============================================================
      {
        path: 'reportes',
        loadComponent: () => import('./features/reportes/reportes')
          .then(m => m.ReportesComponent),
        canActivate: [AuthGuard],
        children: [
          {
            path: 'rentabilidad',
            loadComponent: () => import('./features/reportes/rentabilidad/rentabilidad/rentabilidad')
              .then(m => m.RentabilidadComponent)
          },
          {
            path: 'digemit',
            loadComponent: () => import('./features/reportes/digemit/digemit/digemit')
              .then(m => m.DigemitComponent)
          },
          {
            path: '',
            redirectTo: 'rentabilidad',
            pathMatch: 'full'
          }
        ]
      }, {

        path: 'estadisticas',
        loadComponent: () => import('./features/estadisticas/dashboard/dashboard').then(m => m.DashboardEstadisticasComponent)
      },
      {
        path: 'auditoria',
        loadComponent: () => import('./features/auditoria/auditoria/auditoria')
          .then(m => m.AuditoriaComponent),
        canActivate: [AuthGuard]
      },

      // ============================================================
      // CAJA Y USUARIOS (ya estaban bien, sin cambios)
      // ============================================================
      {
        path: 'caja',
        loadComponent: () => import('./features/caja/listar/listar').then(m => m.ListarComponent)
      },
      {
        path: 'usuarios',
        canActivate: [AuthGuard],
        data: { roles: ['admin'] }, // solo administradores
        loadComponent: () => import('./features/usuarios/listar/listar').then(m => m.ListarUsuariosComponent)
      },
      {
        path: 'configuracion/seguridad',
        loadComponent: () => import('./features/configuracion/seguridad/configuracion-seguridad/configuracion-seguridad')
          .then(m => m.ConfiguracionSeguridadComponent),
        canActivate: [AuthGuard]
      },

      // ============================================================
      // REDIRECCIÓN POR DEFECTO
      // ============================================================
      { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
      {
        path: 'alertas',
        loadComponent: () => import('./features/alertas/alertas/alertas').then(m => m.Alertas)
      },

    ]
  },

  // ================================================================
  // RUTA COMODÍN (404)
  // ================================================================
  { path: '**', redirectTo: '/dashboard' }
];