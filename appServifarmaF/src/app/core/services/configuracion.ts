// src/app/core/services/configuracion.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ConfiguracionSeguridad } from '../models/configuracion.model';

@Injectable({ providedIn: 'root' })
export class ConfiguracionService {
  private apiUrl = `${environment.apiUrl}/configuracion`;

  constructor(private http: HttpClient) {}

  obtenerConfiguracion(): Observable<ConfiguracionSeguridad> {
    return this.http.get<ConfiguracionSeguridad>(`${this.apiUrl}/seguridad`);
  }

  actualizarConfiguracion(config: Partial<ConfiguracionSeguridad>): Observable<any> {
    return this.http.put(`${this.apiUrl}/seguridad`, config);
  }
}