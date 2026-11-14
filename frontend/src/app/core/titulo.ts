import { Injectable, effect, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterStateSnapshot, TitleStrategy } from '@angular/router';
import { idioma, t } from './i18n/i18n';

/** Traduce el título de cada ruta (escrito en español en app.routes.ts) y lo actualiza al cambiar el idioma. */
@Injectable({ providedIn: 'root' })
export class TituloTraducido extends TitleStrategy {
  private readonly title = inject(Title);
  private ultimo: string | undefined;

  constructor() {
    super();
    effect(() => {
      idioma();
      if (this.ultimo) this.title.setTitle(t(this.ultimo));
    });
  }

  override updateTitle(estado: RouterStateSnapshot): void {
    this.ultimo = this.buildTitle(estado);
    if (this.ultimo) this.title.setTitle(t(this.ultimo));
  }
}
