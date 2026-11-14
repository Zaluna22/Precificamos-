import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { simboloMoneda } from '../../core/formato';
import { Configuracion } from '../../core/modelos';
import { TraducirPipe, t } from '../../core/i18n/i18n';
import { SelectorIdioma } from '../../componentes/selector-idioma';

/** Ajustes generales del negocio que se usan como valores por defecto en todos los cálculos. */
@Component({
  selector: 'app-configuracion',
  imports: [FormsModule, TraducirPipe, SelectorIdioma],
  templateUrl: './configuracion.html',
})
export class ConfiguracionPagina {
  private readonly api = inject(ApiService);

  /** Símbolo de la moneda del idioma actual. */
  protected get simbolo(): string { return simboloMoneda(); }
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly mensaje = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);
  protected config: Configuracion = {
    valorHora: 0, impostoPct: 0, taxaCartaoPct: 0, taxaMarketplacePct: 0, comissaoPct: 0, totalUnidadesMes: 0,
  };

  constructor() {
    this.api.configuracion().subscribe({
      next: (c) => { this.config = { ...this.config, ...c }; this.cargando.set(false); },
      error: (e) => { this.error.set(e.erro); this.cargando.set(false); },
    });
  }

  /** Calcula el valor hora a partir de lo que se quiere ganar por mes y las horas trabajadas. */
  protected sueldoDeseado: number | null = null;
  protected horasMes: number | null = 160;

  protected calcularValorHora(): void {
    if (this.sueldoDeseado && this.horasMes) {
      this.config.valorHora = Math.round((this.sueldoDeseado / this.horasMes) * 100) / 100;
    }
  }

  protected guardar(): void {
    this.guardando.set(true);
    this.mensaje.set(null);
    this.error.set(null);
    const limpio: Configuracion = {
      valorHora: Number(this.config.valorHora) || 0,
      impostoPct: Number(this.config.impostoPct) || 0,
      taxaCartaoPct: Number(this.config.taxaCartaoPct) || 0,
      taxaMarketplacePct: Number(this.config.taxaMarketplacePct) || 0,
      comissaoPct: Number(this.config.comissaoPct) || 0,
      totalUnidadesMes: Math.max(0, Math.round(Number(this.config.totalUnidadesMes) || 0)),
    };
    this.api.guardarConfiguracion(limpio).subscribe({
      next: (c) => {
        this.config = c;
        this.guardando.set(false);
        this.mensaje.set(t('Listo, guardamos tu configuración.'));
        this.api.refrescarSegmentos();
      },
      error: (e) => { this.error.set(e.erro); this.guardando.set(false); },
    });
  }
}
