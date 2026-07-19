import { Component, OnInit } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router'; // ✅ Importar Router
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
    private authService: AuthService,
    private router: Router 
  ) { }

  ngOnInit(): void {
    const token = this.authService.getToken();
    if (!token) {
      this.router.navigate(['/login']);
    }
  }
}