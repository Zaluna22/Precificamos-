import { Component, input } from '@angular/core';
import { IDIOMAS, Idioma, TraducirPipe, cambiarIdioma, idioma } from '../core/i18n/i18n';

/** Selector de idioma. Variante "barra" (compacta, para el encabezado) o "campo" (para Configuración). */
@Component({
  selector: 'app-selector-idioma',
  imports: [TraducirPipe],
  template: `
    @if (variante() === 'barra') {
      <label class="selector-idioma">
        <span class="sr-only">{{ 'Idioma' | t }}</span>
        <span aria-hidden="true">🌐</span>
        <select [value]="idioma()" (change)="cambiar($any($event.target).value)" [attr.aria-label]="'Idioma' | t">
          @for (i of idiomas; track i.codigo) {
            <option [value]="i.codigo" [selected]="i.codigo === idioma()" [attr.lang]="i.codigo">{{ i.corto }}</option>
          }
        </select>
      </label>
    } @else {
      <div class="campo">
        <label for="selector-idioma">{{ 'Idioma' | t }}</label>
        <select id="selector-idioma" [value]="idioma()" (change)="cambiar($any($event.target).value)">
          @for (i of idiomas; track i.codigo) {
            <option [value]="i.codigo" [selected]="i.codigo === idioma()" [attr.lang]="i.codigo">{{ i.nombre }}</option>
          }
        </select>
        <small class="ayuda">{{ 'En inglés los montos se muestran en dólares (US$).' | t }}</small>
      </div>
    }
  `,
})
export class SelectorIdioma {
  readonly variante = input<'barra' | 'campo'>('barra');
  protected readonly idiomas = IDIOMAS;
  protected readonly idioma = idioma;

  protected cambiar(valor: string): void {
    cambiarIdioma(valor as Idioma);
  }
}
