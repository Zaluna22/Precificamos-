import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { DefinicionSegmento } from '../../core/modelos';

/** Normaliza texto para buscar sin acentos ni mayúsculas. */
const normalizar = (s: string) => s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();

/** Inicio: el usuario elige la categoría reconociendo su negocio entre los ejemplos. */
@Component({
  selector: 'app-inicio',
  imports: [RouterLink, FormsModule],
  templateUrl: './inicio.html',
})
export class Inicio {
  private readonly api = inject(ApiService);

  protected readonly segmentos = signal<DefinicionSegmento[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly busqueda = signal('');

  /** Categorías filtradas: coincide el nombre, la descripción o algún negocio de ejemplo. */
  protected readonly filtrados = computed(() => {
    const q = normalizar(this.busqueda().trim());
    if (!q) return this.segmentos().map((s) => ({ s, coincidencias: [] as string[] }));
    return this.segmentos()
      .map((s) => ({ s, coincidencias: s.ejemplos.filter((e) => normalizar(e).includes(q)) }))
      .filter(({ s, coincidencias }) =>
        coincidencias.length > 0 || normalizar(s.nombre).includes(q) || normalizar(s.descripcion).includes(q));
  });

  constructor() {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.api.segmentos().subscribe({
      next: (s) => { this.segmentos.set(s); this.cargando.set(false); },
      error: (e) => { this.error.set(e.erro); this.cargando.set(false); },
    });
  }
}
