import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
  usuario: string = '';
  rol: string = '';

  // ==============================
  // KPIs (ADMIN)
  // ==============================
  kpis = [
    { label: 'Ventas del Día', displayValue: 'S/ 2,450.00', change: '+12% vs ayer', positive: true, icon: 'bi-cart3', color: '#16a34a' },
    { label: 'Caja Actual', displayValue: 'S/ 5,320.00', change: '+8% vs ayer', positive: true, icon: 'bi-wallet2', color: '#2563eb' },
    { label: 'Productos Críticos', displayValue: '12', change: '-2 vs ayer', positive: false, icon: 'bi-exclamation-triangle-fill', color: '#dc2626' },
    { label: 'Próximos a Vencer', displayValue: '8', change: '+3 vs ayer', positive: true, icon: 'bi-archive-fill', color: '#f59e0b' },
    { label: 'Demanda Insatisfecha', displayValue: '5', change: '+1 vs ayer', positive: true, icon: 'bi-graph-down-arrow', color: '#ea580c' },
    { label: 'Productos Totales', displayValue: '342', change: '+15 vs ayer', positive: true, icon: 'bi-boxes', color: '#7c3aed' },
  ];

  // ==============================
  // VENTAS RECIENTES (ADMIN)
  // ==============================
  ventasRecientes = [
    { cliente: 'María González', productos: 3, hora: '10:30 AM', total: 45.50 },
    { cliente: 'Juan Pérez', productos: 1, hora: '11:15 AM', total: 12.00 },
    { cliente: 'Ana Torres', productos: 5, hora: '11:45 AM', total: 89.90 },
    { cliente: 'Carlos Ruiz', productos: 2, hora: '12:20 PM', total: 38.00 }
  ];

  // ==============================
  // STOCK CRÍTICO (ADMIN)
  // ==============================
  stockCritico = [
    { producto: 'Paracetamol 500mg', stock: 8, minimo: 20 },
    { producto: 'Ibuprofeno 400mg', stock: 5, minimo: 15 },
    { producto: 'Amoxicilina 500mg', stock: 12, minimo: 25 }
  ];

  // ==============================
  // ACCESOS RÁPIDOS (VENDEDOR)
  // ==============================
  accesosRapidos = [
    { titulo: 'Nueva Venta', descripcion: 'Registrar una nueva venta', icon: 'bi-cart-plus', color: '#2563eb', ruta: '/ventas' },
    { titulo: 'Buscar Producto', descripcion: 'Búsqueda visual de productos', icon: 'bi-search', color: '#16a34a', ruta: '/buscador' },
    { titulo: 'Consultar Stock', descripcion: 'Ver inventario disponible', icon: 'bi-box-seam', color: '#f59e0b', ruta: '/inventario' },
    { titulo: 'Registrar Cliente', descripcion: 'Agregar nuevo cliente', icon: 'bi-person-plus-fill', color: '#0891b2', ruta: '/clientes' },
    { titulo: 'Demanda Insatisfecha', descripcion: 'Registrar solicitudes', icon: 'bi-exclamation-diamond-fill', color: '#dc2626', ruta: '/demanda' },
  ];

  // ==============================
  // ACTIVIDAD RECIENTE (VENDEDOR)
  // ==============================
  actividadReciente = [
    { descripcion: 'Venta realizada', cliente: 'María González', hora: '10:30 AM', monto: 45.50 },
    { descripcion: 'Cliente registrado', cliente: 'Juan Pérez', hora: '11:15 AM', monto: 0 },
    { descripcion: 'Venta realizada', cliente: 'Ana Torres', hora: '11:45 AM', monto: 89.90 },
    { descripcion: 'Demanda registrada', cliente: 'Carlos Ruiz', hora: '12:20 PM', monto: 0 }
  ];

  // ==============================
  // ALERTAS (compartidas por ambos roles)
  // ==============================
  stockMinimoAlertas = [
    { producto: 'Paracetamol 500mg', stock: 8 },
    { producto: 'Ibuprofeno 400mg', stock: 5 }
  ];

  proximosVencerAlertas = [
    { producto: 'Amoxicilina 500mg', dias: 15 },
    { producto: 'Loratadina 10mg', dias: 20 }
  ];

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    this.usuario = this.authService.getUsuario() || 'Usuario';
    this.rol = (this.authService.getRol() || '').toUpperCase();
  }
}