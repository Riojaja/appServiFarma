import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';  // <-- Agregar esta importación

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],  // <-- Agregar RouterOutlet aquí
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  title = 'appServifarmaF';
}