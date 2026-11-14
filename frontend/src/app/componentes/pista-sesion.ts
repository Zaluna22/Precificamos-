import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { TraducirPipe } from '../core/i18n/i18n';

/** Aviso suave para quien usa la calculadora sin cuenta: entrar le guarda los costos. */
@Component({
  selector: 'app-pista-sesion',
  imports: [RouterLink, TraducirPipe],
  template: `
    @if (!auth.logueado()) {
      <aside class="alerta pista-sesion">
        <p>💡 {{ 'Podés calcular sin cuenta. Si entrás, guardamos tus costos fijos y tu configuración para tus próximos cálculos.' | t }}</p>
        <a class="boton boton-secundario" routerLink="/entrar" [queryParams]="{ volver: router.url }">{{ 'Entrar' | t }}</a>
      </aside>
    }
  `,
})
export class PistaSesion {
  protected readonly auth = inject(AuthService);
  protected readonly router = inject(Router);
}
