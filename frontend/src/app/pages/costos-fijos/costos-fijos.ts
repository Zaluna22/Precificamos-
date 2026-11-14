import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { FormatoPipe, simboloMoneda } from '../../core/formato';
import { CostoFijo } from '../../core/modelos';
import { TraducirPipe, t } from '../../core/i18n/i18n';

/** Sugerencias para que el usuario no se olvide de ningún costo fijo (en español; se muestran traducidas). */
const SUGERENCIAS = ['Alquiler', 'Luz', 'Agua', 'Internet y celular', 'Contador', 'DAS del MEI', 'Sistema / app de gestión',
  'Publicidad', 'Sueldos fijos', 'Mantenimiento de equipos'];

/** Lista, alta y baja de costos fijos mensuales. */
@Component({
  selector: 'app-costos-fijos',
  imports: [FormsModule, FormatoPipe, TraducirPipe],
  templateUrl: './costos-fijos.html',
})
export class CostosFijos {
  private readonly api = inject(ApiService);

  /** Símbolo de la moneda del idioma actual. */
  protected get simbolo(): string { return simboloMoneda(); }
  protected readonly costos = signal<CostoFijo[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly guardando = signal(false);
  protected readonly total = computed(() => this.costos().reduce((s, c) => s + Number(c.valorMensal || 0), 0));
  protected readonly sugerencias = computed(() => {
    const usados = new Set(this.costos().map((c) => c.nome.toLowerCase()));
    return SUGERENCIAS.filter((s) => !usados.has(s.toLowerCase()) && !usados.has(t(s).toLowerCase()));
  });
  protected nuevo: CostoFijo = { nome: '', valorMensal: null as unknown as number };

  constructor() {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.api.costosFijos().subscribe({
      next: (c) => { this.costos.set(c); this.cargando.set(false); this.error.set(null); },
      error: (e) => { this.error.set(e.erro); this.cargando.set(false); },
    });
  }

  protected usarSugerencia(nombre: string): void {
    this.nuevo = { ...this.nuevo, nome: t(nombre) };
    document.getElementById('costo-valor')?.focus();
  }

  protected agregar(): void {
    const nome = this.nuevo.nome?.trim();
    const valor = Number(this.nuevo.valorMensal);
    if (!nome || !(valor >= 0)) {
      this.error.set(t('Poné un nombre y un valor mensual.'));
      return;
    }
    this.guardando.set(true);
    this.api.crearCostoFijo({ nome, valorMensal: valor }).subscribe({
      next: (c) => {
        this.costos.update((l) => [...l, c]);
        this.nuevo = { nome: '', valorMensal: null as unknown as number };
        this.guardando.set(false);
        this.error.set(null);
        this.api.refrescarSegmentos();
      },
      error: (e) => { this.error.set(e.erro); this.guardando.set(false); },
    });
  }

  protected borrar(c: CostoFijo): void {
    if (c.id === undefined || !confirm(t('¿Borrar “{0}”?', c.nome))) return;
    this.api.borrarCostoFijo(c.id).subscribe({
      next: () => { this.costos.update((l) => l.filter((x) => x.id !== c.id)); this.api.refrescarSegmentos(); },
      error: (e) => this.error.set(e.erro),
    });
  }
}
