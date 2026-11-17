import { Component, effect, inject, input, output, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { simboloMoneda } from '../core/formato';
import { TraducirPipe, t } from '../core/i18n/i18n';
import { ProductoGuardado, ResultadoSegmento, SolicitudCalculo } from '../core/modelos';
import { NumeroDecimal } from '../core/numero-decimal';

/** Clave para volver a calcular solo después de crear la cuenta o entrar (así no se pierde el resultado). */
export const CLAVE_RECALCULAR = 'precificamos.recalcular';

/**
 * Guarda el resultado de la calculadora en "Mis productos". Sin sesión invita a crear una cuenta.
 * Si ya se guardó (o se está editando un producto guardado), guardar de nuevo actualiza el mismo.
 */
@Component({
  selector: 'app-guardar-producto',
  imports: [FormsModule, RouterLink, NumeroDecimal, TraducirPipe],
  template: `
    @if (!auth.logueado()) {
      <aside class="tarjeta guardar-producto guardar-anonimo">
        <p>💾 {{ 'Creá una cuenta gratis para guardar este producto y armar tu lista de precios.' | t }}</p>
        <div class="acciones-guardar">
          <a class="boton" routerLink="/registro" [queryParams]="{ volver: router.url }" (click)="recordarRecalculo()">{{ 'Crear cuenta para guardar' | t }}</a>
          <a class="boton boton-texto" routerLink="/entrar" [queryParams]="{ volver: router.url }" (click)="recordarRecalculo()">{{ 'Ya tengo cuenta' | t }}</a>
        </div>
      </aside>
    } @else {
      <form class="tarjeta formulario guardar-producto" (ngSubmit)="guardar()" novalidate>
        <h2>{{ (productoId() ? 'Guardar cambios del producto' : 'Guardar producto') | t }}</h2>
        <div class="fila-campos">
          <div class="campo">
            <label for="guardar-nombre">{{ 'Nombre del producto' | t }}</label>
            <input id="guardar-nombre" name="nombre" type="text" maxlength="120" [(ngModel)]="nombre" autocomplete="off" />
          </div>
          <div class="campo">
            <label for="guardar-precio">{{ 'Precio final' | t }}</label>
            <div class="entrada-numero">
              <span class="prefijo" aria-hidden="true">{{ simbolo }}</span>
              <input id="guardar-precio" name="precioFinal" type="text" decimal inputmode="decimal" min="0" step="any"
                     [(ngModel)]="precioFinal" />
            </div>
            <span class="ayuda">{{ 'Es el precio con el que vas a vender. Arranca en el sugerido o en el que probaste en el simulador.' | t }}</span>
          </div>
        </div>
        @if (error()) { <p class="alerta alerta-error" role="alert">{{ error() }}</p> }
        @if (guardadoOk()) {
          <p class="alerta alerta-ok" role="status">
            ✅ {{ 'Guardado en Mis productos.' | t }} <a routerLink="/meus-produtos">{{ 'Ver mis productos' | t }}</a>
          </p>
        }
        <button type="submit" class="boton" [disabled]="guardando()">
          {{ (guardando() ? 'Guardando…' : (productoId() ? 'Guardar cambios' : 'Guardar producto')) | t }}
        </button>
      </form>
    }
  `,
})
export class GuardarProducto {
  protected readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  protected readonly router = inject(Router);

  readonly codigo = input.required<string>();
  readonly solicitud = input.required<SolicitudCalculo>();
  readonly resultado = input.required<ResultadoSegmento>();
  /** Precio elegido en el simulador (o el guardado, al editar). */
  readonly precioElegido = input<number | null>(null);
  /** Id del producto guardado que se está editando; null si es nuevo. */
  readonly productoId = input<number | null>(null);
  /** Nombre guardado (al editar) para no pisarlo con el del formulario. */
  readonly nombreGuardado = input<string | null>(null);
  readonly guardado = output<ProductoGuardado>();

  protected nombre = '';
  protected precioFinal: number | null = null;
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly guardadoOk = signal(false);

  protected get simbolo(): string { return simboloMoneda(); }

  constructor() {
    // Nombre y precio se completan con el resultado; el precio sigue al simulador.
    effect(() => {
      const r = this.resultado();
      const guardado = this.nombreGuardado();
      untracked(() => {
        if (!this.nombre) this.nombre = guardado || r.nombre || '';
        if (this.precioFinal === null) this.precioFinal = this.precioElegido() ?? r.precioSugerido;
      });
    });
    effect(() => {
      const p = this.precioElegido();
      if (p !== null && p > 0) untracked(() => { this.precioFinal = p; this.guardadoOk.set(false); });
    });
  }

  protected recordarRecalculo(): void {
    try { sessionStorage.setItem(CLAVE_RECALCULAR, this.codigo()); } catch { /* modo privado */ }
  }

  protected guardar(): void {
    const nombre = this.nombre.trim();
    const precio = Number(this.precioFinal);
    if (!nombre) { this.error.set(t('Poné un nombre al producto')); return; }
    if (this.precioFinal === null || String(this.precioFinal).trim() === '' || !(precio >= 0)) {
      this.error.set(t('Poné un precio final válido'));
      return;
    }
    const r = this.resultado();
    const s = this.solicitud();
    const cuerpo = {
      segmento: this.codigo(),
      nombre,
      comun: s.comun,
      datos: s.datos,
      precioSugerido: r.precioSugerido,
      costoTotalUnitario: r.costoTotalUnitario,
      margenRealPct: r.margenRealPct,
      lucroUnitario: r.lucroUnitario,
      precioFinal: precio,
    };
    const id = this.productoId();
    this.guardando.set(true);
    this.error.set(null);
    (id ? this.api.actualizarProducto(id, cuerpo) : this.api.guardarProducto(cuerpo)).subscribe({
      next: (p) => {
        this.guardando.set(false);
        this.guardadoOk.set(true);
        this.guardado.emit(p);
      },
      error: (e) => { this.guardando.set(false); this.error.set(e.erro); },
    });
  }
}
