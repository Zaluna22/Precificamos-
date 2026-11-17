import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, from, map, of, shareReplay, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { Idioma, idioma, t } from './i18n/i18n';
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
  /** Idioma con el que se pidió la caché de categorías (los textos vienen traducidos de la API). */
  private idiomaSegmentos?: string;

  /**
   * Categorías con ejemplos y formularios (se cachea durante la sesión, por idioma).
   *
   * Sin sesión se muestran al instante desde una copia incluida en la app, así el inicio no espera
   * a que el servidor gratuito despierte. Mientras tanto se piden las categorías a la API en
   * segundo plano: eso despierta el servidor y deja la versión viva para la próxima vez.
   * Con sesión se piden siempre a la API, porque los valores por defecto dependen de la configuración.
   */
  segmentos(): Observable<DefinicionSegmento[]> {
    if (this.idiomaSegmentos !== idioma()) {
      this.segmentos$ = undefined;
      this.idiomaSegmentos = idioma();
    }
    if (!this.segmentos$) {
      if (haySesion()) {
        this.segmentos$ = this.segmentosVivos().pipe(
          catchError((e) => { this.segmentos$ = undefined; return this.error(e); }),
          shareReplay(1),
        );
      } else {
        const lang = idioma();
        this.segmentos$ = from(FOTOS[lang]()).pipe(map((m) => m.default as DefinicionSegmento[]), shareReplay(1));
        this.segmentosVivos().subscribe({
          next: (s) => { if (this.idiomaSegmentos === lang && !haySesion()) this.segmentos$ = of(s); },
          error: () => { /* nos quedamos con la copia incluida */ },
        });
      }
    }
    return this.segmentos$;
  }

  private segmentosVivos(): Observable<DefinicionSegmento[]> {
    return this.http.get<DefinicionSegmento[]>(`${this.base}/segmentos`);
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

/**
 * Copia de GET /api/segmentos por idioma, incluida en la app para que el inicio cargue al instante.
 * Se regenera con: npm run segmentos:foto (con el servidor despierto).
 */
const FOTOS: Record<Idioma, () => Promise<{ default: unknown }>> = {
  es: () => import('./segmentos-foto/es.json'),
  'pt-BR': () => import('./segmentos-foto/pt-BR.json'),
  en: () => import('./segmentos-foto/en.json'),
};

/** Misma clave que usa AuthService (no se inyecta para evitar una dependencia circular). */
function haySesion(): boolean {
  try { return localStorage.getItem('precificamos.token') !== null; } catch { return false; }
}

/** Normaliza cualquier error HTTP a {@link ErrorApi} con un mensaje entendible. */
export function errorApi(e: HttpErrorResponse): Observable<never> {
  let err: ErrorApi;
  if (e.status === 0) {
    err = { erro: t('No pudimos conectar con el servidor. Revisá tu conexión o probá de nuevo en un minuto (el servidor gratuito puede tardar en despertar).') };
  } else if (e.error && typeof e.error === 'object' && 'erro' in e.error) {
    err = e.error as ErrorApi;
  } else {
    err = { erro: t('Ocurrió un error inesperado ({0}).', e.status) };
  }
  return throwError(() => err);
}
