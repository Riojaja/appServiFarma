import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ReniecResponse {
  success: boolean;
  data?: {
    dni: string;
    nombres: string;
    apellidoPaterno: string;
    apellidoMaterno: string;
    nombreCompleto: string;
  };
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReniecService {
  private apiUrl = `${environment.apiUrl}/reniec`;

  constructor(private http: HttpClient) { }

  consultarPorDni(dni: string): Observable<ReniecResponse> {
    return this.http.get<ReniecResponse>(`${this.apiUrl}/consultar?dni=${dni}`);
  }
}