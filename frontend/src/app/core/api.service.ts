import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, shareReplay, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Configuracion, CostoFijo, DefinicionSegmento, ErrorApi, ResultadoSegmento, ResultadoSimulacion,
  SolicitudCalculo,
} from './modelos';

/** Acceso al backend de Precificamos. La URL base sale del archivo de entorno. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api`;
  private segmentos$?: Observable<DefinicionSegmento[]>;

  /** Categorías con ejemplos y formularios (se cachea durante la sesión). */
  segmentos(): Observable<DefinicionSegmento[]> {
    this.segmentos$ ??= this.http.get<DefinicionSegmento[]>(`${this.base}/segmentos`).pipe(
      catchError((e) => { this.segmentos$ = undefined; return this.error(e); }),
      shareReplay(1),
    );
    return this.segmentos$;
  }

  segmento(codigo: string): Observable<DefinicionSegmento> {
    return this.http.get<DefinicionSegmento>(`${this.base}/segmentos/${codigo}`).pipe(catchError((e) => this.error(e)));
  }

  calcular(codigo: string, solicitud: SolicitudCalculo): Observable<ResultadoSegmento> {
    return this.http.post<ResultadoSegmento>(`${this.base}/segmentos/${codigo}/calcular`, solicitud)
      .pipe(catchError((e) => this.error(e)));
  }

  simular(codigo: string, solicitud: SolicitudCalculo): Observable<ResultadoSimulacion> {
    return this.http.post<ResultadoSimulacion>(`${this.base}/segmentos/${codigo}/simular`, solicitud)
      .pipe(catchError((e) => this.error(e)));
  }

  costosFijos(): Observable<CostoFijo[]> {
    return this.http.get<CostoFijo[]>(`${this.base}/custos-fixos`).pipe(catchError((e) => this.error(e)));
  }

  crearCostoFijo(c: CostoFijo): Observable<CostoFijo> {
    return this.http.post<CostoFijo>(`${this.base}/custos-fixos`, c).pipe(catchError((e) => this.error(e)));
  }

  borrarCostoFijo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/custos-fixos/${id}`).pipe(catchError((e) => this.error(e)));
  }

  configuracion(): Observable<Configuracion> {
    return this.http.get<Configuracion>(`${this.base}/configuracao`).pipe(catchError((e) => this.error(e)));
  }

  guardarConfiguracion(c: Configuracion): Observable<Configuracion> {
    return this.http.put<Configuracion>(`${this.base}/configuracao`, c).pipe(catchError((e) => this.error(e)));
  }

  /** Invalida la caché de categorías (los valores por defecto dependen de la configuración). */
  refrescarSegmentos(): void {
    this.segmentos$ = undefined;
  }

  private error(e: HttpErrorResponse): Observable<never> {
    return errorApi(e);
  }
}

/** Normaliza cualquier error HTTP a {@link ErrorApi} con un mensaje entendible. */
export function errorApi(e: HttpErrorResponse): Observable<never> {
  let err: ErrorApi;
  if (e.status === 0) {
    err = { erro: 'No pudimos conectar con el servidor. Revisá tu conexión o probá de nuevo en un minuto (el servidor gratuito puede tardar en despertar).' };
  } else if (e.error && typeof e.error === 'object' && 'erro' in e.error) {
    err = e.error as ErrorApi;
  } else {
    err = { erro: `Ocurrió un error inesperado (${e.status}).` };
  }
  return throwError(() => err);
}
