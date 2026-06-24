import { Routes } from '@angular/router';
import { LayoutComponent } from './core/layout/layout';
import { LoginComponent } from './features/auth/login/login';
import { DashboardComponent } from './features/dashboard/dashboard/dashboard';
import { AuthGuard } from './core/auth-guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: '', redirectTo: '/dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '/dashboard' }
];