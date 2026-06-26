import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../auth';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css']
})
export class SidebarComponent implements OnInit {
  nombreCompleto: string = '';
  usuario: string = '';
  rol: string = '';
  isAdmin: boolean = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.usuario = this.authService.getUsuario() || '';
    this.nombreCompleto = this.authService.getNombreCompleto?.() || this.usuario;
    const rolRaw = this.authService.getRol() || '';
    this.rol = rolRaw.charAt(0).toUpperCase() + rolRaw.slice(1).toLowerCase();
    this.isAdmin = rolRaw.toUpperCase() === 'ADMIN';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}