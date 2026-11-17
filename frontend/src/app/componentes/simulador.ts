import { Component, DestroyRef, OnInit, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { NumeroDecimal } from '../core/numero-decimal';
import { Subject, catchError, debounceTime, of, switchMap } from 'rxjs';
import { ApiService } from '../core/api.service';
import { FormatoPipe, simboloMoneda } from '../core/formato';
import { TraducirPipe } from '../core/i18n/i18n';
import { ResultadoSimulacion, SolicitudCalculo } from '../core/modelos';

/** Simulador: el usuario mueve el precio y las ventas y ve al instante su ganancia. */
@Component({
  selector: 'app-simulador',
  imports: [FormsModule, NumeroDecimal, FormatoPipe, TraducirPipe],
  templateUrl: './simulador.html',
})
export class Simulador implements OnInit {
  private readonly api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly codigo = input.required<string>();
  readonly solicitud = input.required<SolicitudCalculo>();
  readonly precioSugerido = input.required<number>();
  readonly ventasIniciales = input<number>(1);
  /** Avisa el precio que el usuario va probando (se usa como precio final al guardar el producto). */
  readonly precioElegido = output<number>();

  protected readonly precio = signal(0);
  protected readonly ventas = signal(1);
  protected readonly resultado = signal<ResultadoSimulacion | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly cargando = signal(false);
  private readonly cambios = new Subject<void>();

  /** Símbolo de la moneda del idioma actual. */
  protected get simbolo(): string { return simboloMoneda(); }

  protected get minimo(): number { return Math.max(0.01, Math.floor(this.precioSugerido() * 0.4 * 100) / 100); }
  protected get maximo(): number { return Math.ceil(this.precioSugerido() * 2); }
  protected get pasoSlider(): number { return this.precioSugerido() >= 100 ? 1 : 0.05; }

  ngOnInit(): void {
    this.precio.set(this.precioSugerido());
    this.ventas.set(this.ventasIniciales());
    this.cambios.pipe(
      debounceTime(300),
      switchMap(() => {
        const precio = Number(this.precio());
        const ventas = Math.max(1, Math.round(Number(this.ventas()) || 1));
        if (!(precio > 0)) return of(null);
        this.cargando.set(true);
        const s = this.solicitud();
        return this.api.simular(this.codigo(), { ...s, comun: { ...s.comun, ventasMensualesProducto: ventas }, precio })
          .pipe(catchError((e) => { this.error.set(e.erro); return of(null); }));
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((r) => {
      this.cargando.set(false);
      if (r) { this.resultado.set(r); this.error.set(null); }
    });
    this.cambios.next();
  }

  protected cambiarPrecio(v: number): void {
    this.precio.set(Number(v));
    this.cambios.next();
    if (Number(v) > 0) this.precioElegido.emit(Number(v));
  }
  protected cambiarVentas(v: number): void { this.ventas.set(Number(v)); this.cambios.next(); }
  protected volverAlSugerido(): void { this.cambiarPrecio(this.precioSugerido()); }
}
