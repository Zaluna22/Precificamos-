import { Pipe, PipeTransform } from '@angular/core';
import { environment } from '../../environments/environment';

const moneda = new Intl.NumberFormat(environment.localeMoneda, { style: 'currency', currency: environment.moneda });
const numero = new Intl.NumberFormat('es', { maximumFractionDigits: 2 });

/** Formatea un valor según su tipo de línea de desglose. */
export function formatear(valor: number | null | undefined, tipo: string = 'moneda'): string {
  if (valor === null || valor === undefined || Number.isNaN(valor)) return '—';
  switch (tipo) {
    case 'moneda': return moneda.format(valor);
    case 'porcentaje': return `${numero.format(valor)} %`;
    case 'dias': return `${numero.format(valor)} días`;
    default: return numero.format(valor);
  }
}

/** Uso en plantillas: {{ valor | fmt }} o {{ valor | fmt: 'porcentaje' }}. */
@Pipe({ name: 'fmt' })
export class FormatoPipe implements PipeTransform {
  transform(valor: number | null | undefined, tipo: string = 'moneda'): string {
    return formatear(valor, tipo);
  }
}

/** Símbolo de la moneda configurada (R$, $, €…). */
export function simboloMoneda(): string {
  return moneda.formatToParts(0).find((p) => p.type === 'currency')?.value ?? '$';
}
