import { Component, input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CampoFormulario, Opcion } from '../core/modelos';
import { formatear, simboloMoneda } from '../core/formato';
import { TraducirPipe } from '../core/i18n/i18n';

/** Fila vacía de una lista con los valores por defecto de sus columnas. */
export function filaVacia(campo: CampoFormulario): Record<string, unknown> {
  const fila: Record<string, unknown> = {};
  for (const sub of campo.subcampos ?? []) fila[sub.nombre] = sub.valorPorDefecto ?? null;
  return fila;
}

/**
 * Dibuja un campo del formulario a partir de su descripción en la API (texto, número, moneda,
 * porcentaje, sí/no, selección o lista de filas). Las listas se dibujan de forma recursiva.
 */
@Component({
  selector: 'app-campo-dinamico',
  imports: [FormsModule, CampoDinamico, TraducirPipe],
  templateUrl: './campo-dinamico.html',
})
export class CampoDinamico {
  readonly campo = input.required<CampoFormulario>();
  /** Objeto donde se guarda el valor (datos, comun o una fila de lista). */
  readonly modelo = input.required<Record<string, unknown>>();
  /** Clave del campo para buscar su error, por ejemplo "datos.ingredientes[0].precioCompra". */
  readonly clave = input.required<string>();
  readonly errores = input<Record<string, string>>({});

  /** Símbolo de la moneda del idioma actual. */
  protected get simbolo(): string { return simboloMoneda(); }

  /** Texto extra de una opción: el margen de referencia de una categoría de góndola. */
  protected detalleOpcion(o: Opcion): string {
    return typeof o.extra === 'number' ? ` (${formatear(o.extra, 'porcentaje')})` : '';
  }

  protected get id(): string {
    return 'campo-' + this.clave().replace(/[^a-zA-Z0-9]/g, '-');
  }

  protected filas(): Record<string, unknown>[] {
    const m = this.modelo();
    const nombre = this.campo().nombre;
    if (!Array.isArray(m[nombre])) m[nombre] = [];
    return m[nombre] as Record<string, unknown>[];
  }

  protected agregarFila(): void {
    this.filas().push(filaVacia(this.campo()));
  }

  protected quitarFila(i: number): void {
    this.filas().splice(i, 1);
  }

  protected sufijo(): string | null {
    switch (this.campo().tipo) {
      case 'porcentaje': return '%';
      default: return null;
    }
  }

  protected paso(): string {
    return this.campo().tipo === 'entero' ? '1' : 'any';
  }

  protected modoTeclado(): string {
    return this.campo().tipo === 'entero' ? 'numeric' : 'decimal';
  }
}
