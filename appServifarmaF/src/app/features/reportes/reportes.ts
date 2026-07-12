import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterOutlet, Router } from '@angular/router';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterOutlet],
  templateUrl: './reportes.html',
  styleUrls: ['./reportes.css']
})
export class ReportesComponent {
  constructor(private router: Router) {}

  // Método para verificar si la ruta está activa
  isActive(route: string): boolean {
    const currentUrl = this.router.url;
    return currentUrl === `/reportes/${route}` || currentUrl === `/reportes`;
  }
}