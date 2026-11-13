import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';

/** Aviso suave para quien usa la calculadora sin cuenta: entrar le guarda los costos. */
@Component({
  selector: 'app-pista-sesion',
  imports: [RouterLink],
  template: `
    @if (!auth.logueado()) {
      <aside class="alerta pista-sesion">
        <p>💡 Podés calcular sin cuenta. Si entrás, guardamos tus costos fijos y tu configuración para tus próximos cálculos.</p>
        <a class="boton boton-secundario" routerLink="/entrar" [queryParams]="{ volver: router.url }">Entrar</a>
      </aside>
    }
  `,
})
export class PistaSesion {
  protected readonly auth = inject(AuthService);
  protected readonly router = inject(Router);
}
