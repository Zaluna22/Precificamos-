import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiService, errorApi } from './api.service';
import { RespuestaAuth, Usuario } from './modelos';

const CLAVE_TOKEN = 'precificamos.token';
const CLAVE_USUARIO = 'precificamos.usuario';

/**
 * Sesión del usuario con signals. El token se guarda en localStorage para que la sesión
 * sobreviva a recargar la página; el interceptor lo agrega en cada llamada a la API.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly base = `${environment.apiUrl}/api/auth`;

  private readonly _token = signal<string | null>(leer(CLAVE_TOKEN));
  private readonly _usuario = signal<Usuario | null>(leerJson<Usuario>(CLAVE_USUARIO));

  readonly token = this._token.asReadonly();
  readonly usuario = this._usuario.asReadonly();
  readonly logueado = computed(() => this._token() !== null);

  constructor() {
    // Confirma que el token guardado siga siendo válido (si venció, el interceptor cierra la sesión).
    // Se difiere para no llamar al interceptor (que inyecta este servicio) durante la construcción.
    if (this._token()) queueMicrotask(() => this.verificar());
  }

  private verificar(): void {
    this.http.get<Usuario>(`${this.base}/yo`).subscribe({
      next: (u) => this.guardarUsuario(u),
      error: () => { /* 401 ya lo maneja el interceptor; sin conexión se mantiene la sesión */ },
    });
  }

  login(email: string, password: string): Observable<RespuestaAuth> {
    return this.http.post<RespuestaAuth>(`${this.base}/login`, { email, password })
      .pipe(tap((r) => this.iniciar(r)), catchError(errorApi));
  }

  registro(nombre: string, email: string, password: string): Observable<RespuestaAuth> {
    return this.http.post<RespuestaAuth>(`${this.base}/registro`, { nombre, email, password })
      .pipe(tap((r) => this.iniciar(r)), catchError(errorApi));
  }

  /** Cierra la sesión y olvida el token. */
  salir(): void {
    if (this._token() === null && this._usuario() === null) return;
    this._token.set(null);
    this._usuario.set(null);
    borrar(CLAVE_TOKEN);
    borrar(CLAVE_USUARIO);
    // Los valores por defecto del formulario dependen del usuario: se piden de nuevo.
    this.api.refrescarSegmentos();
  }

  private iniciar(r: RespuestaAuth): void {
    this._token.set(r.token);
    escribir(CLAVE_TOKEN, r.token);
    this.guardarUsuario(r.usuario);
    this.api.refrescarSegmentos();
  }

  private guardarUsuario(u: Usuario): void {
    this._usuario.set(u);
    escribir(CLAVE_USUARIO, JSON.stringify(u));
  }
}

/** Ruta interna segura para volver después de entrar (evita redirecciones a otros sitios). */
export function rutaSegura(url: string | null | undefined): string {
  return url && url.startsWith('/') && !url.startsWith('//') ? url : '/';
}

function leer(clave: string): string | null {
  try { return localStorage.getItem(clave); } catch { return null; }
}

function leerJson<T>(clave: string): T | null {
  try {
    const crudo = leer(clave);
    return crudo ? (JSON.parse(crudo) as T) : null;
  } catch {
    return null;
  }
}

function escribir(clave: string, valor: string): void {
  try { localStorage.setItem(clave, valor); } catch { /* sin espacio o modo privado */ }
}

function borrar(clave: string): void {
  try { localStorage.removeItem(clave); } catch { /* ignorar */ }
}
