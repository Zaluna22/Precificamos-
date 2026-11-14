import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { TraducirPipe } from './core/i18n/i18n';
import { SelectorIdioma } from './componentes/selector-idioma';

/** Estructura general: encabezado con la sesión, contenido y navegación inferior en celulares. */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TraducirPipe, SelectorIdioma],
  templateUrl: './app.html',
})
export class App {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly anio = new Date().getFullYear();

  /** Primer nombre para el encabezado (en celulares hay poco lugar). */
  protected primerNombre(): string {
    return this.auth.usuario()?.nombre.trim().split(/\s+/)[0] ?? '';
  }

  protected salir(): void {
    this.auth.salir();
    this.router.navigateByUrl('/');
  }
}
