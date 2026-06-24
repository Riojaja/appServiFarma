import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../auth';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class HeaderComponent implements OnInit {
  usuario: string = '';
  rol: string = '';

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
    this.usuario = this.authService.getUsuario() || 'Usuario';
    this.rol = this.authService.getRol() || 'Sin rol';
  }

  /**
   * Cierra la sesión del usuario y redirige al login.
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  /**
   * Alterna la visibilidad del sidebar en dispositivos móviles.
   * Busca el elemento con id 'wrapper' y le añade/quita la clase 'toggled'.
   */
  toggleSidebar(): void {
    const wrapper = document.getElementById('wrapper');
    if (wrapper) {
      wrapper.classList.toggle('toggled');
    }
  }
}