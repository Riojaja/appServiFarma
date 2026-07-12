import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { InactividadService } from './core/services/inactividad';
import { AuthService } from './core/auth';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent implements OnInit {
  title = 'appServifarmaF';

  constructor(
    private inactividadService: InactividadService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // El servicio de inactividad se inicia automáticamente al ser inyectado.
    // Si el usuario no está autenticado, el servicio no hará nada (ya que en resetTimer verifica isAuthenticated).
    // Pero podemos forzar una verificación inicial si queremos.
  }
}