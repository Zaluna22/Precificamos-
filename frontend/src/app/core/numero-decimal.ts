import { Directive, ElementRef, forwardRef, inject } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { idioma } from './i18n/i18n';

/**
 * Interpreta lo que el usuario escribe como número aceptando coma o punto decimal:
 * "1,5" y "1.5" → 1.5; "1.234,56" y "1,234.56" → 1234.56. Vacío o inválido → null.
 * Con un solo tipo de separador repetido ("1.000.000") se toma como separador de miles.
 * Un separador de miles del idioma seguido de exactamente tres dígitos también es de miles:
 * "1.500" en español y portugués, "1,500" en inglés → 1500.
 */
export function leerNumero(texto: string | null | undefined): number | null {
  let s = (texto ?? '').replace(/[\s\u00a0]/g, '');
  if (!s) return null;
  const coma = s.lastIndexOf(',');
  const punto = s.lastIndexOf('.');
  if (coma >= 0 && punto >= 0) {
    const decimal = coma > punto ? ',' : '.';
    const miles = decimal === ',' ? '.' : ',';
    s = s.split(miles).join('').replace(decimal, '.');
  } else if (coma >= 0 || punto >= 0) {
    const sep = coma >= 0 ? ',' : '.';
    const partes = s.split(sep);
    const esMiles = partes.length > 2
      || (partes[1].length === 3 && sep === (idioma() === 'en' ? ',' : '.'));
    s = esMiles ? partes.join('') : partes.join('.');
  }
  if (!/^-?\d*\.?\d*$/.test(s) || s === '.' || s === '-') return null;
  const n = Number(s);
  return Number.isFinite(n) ? n : null;
}

/** Muestra un número con el separador decimal del idioma (coma en español y portugués). */
function mostrarNumero(n: number): string {
  const texto = String(n);
  return idioma() === 'en' ? texto : texto.replace('.', ',');
}

/**
 * Reemplaza a input type="text" decimal (que en muchos navegadores convierte "1,5" en 15 o lo descarta).
 * Uso: <input type="text" decimal inputmode="decimal" [(ngModel)]="valor" />.
 */
@Directive({
  selector: 'input[decimal]',
  host: { '(input)': 'alEscribir()', '(blur)': 'alSalir()' },
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => NumeroDecimal), multi: true }],
})
export class NumeroDecimal implements ControlValueAccessor {
  private readonly el = inject<ElementRef<HTMLInputElement>>(ElementRef).nativeElement;
  private cambio: (v: number | null) => void = () => {};
  private tocado: () => void = () => {};

  writeValue(v: unknown): void {
    const n = typeof v === 'number' && Number.isFinite(v) ? v : (typeof v === 'string' ? leerNumero(v) : null);
    // No pisar lo que el usuario está escribiendo ("1," todavía vale 1).
    if (n === leerNumero(this.el.value) && this.el.value !== '') return;
    this.el.value = n === null ? '' : mostrarNumero(n);
  }

  registerOnChange(fn: (v: number | null) => void): void { this.cambio = fn; }
  registerOnTouched(fn: () => void): void { this.tocado = fn; }
  setDisabledState(d: boolean): void { this.el.disabled = d; }

  protected alEscribir(): void { this.cambio(leerNumero(this.el.value)); }

  protected alSalir(): void {
    const n = leerNumero(this.el.value);
    this.el.value = n === null ? '' : mostrarNumero(n);
    this.tocado();
  }
}
