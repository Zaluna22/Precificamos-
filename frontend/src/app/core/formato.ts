import { Pipe, PipeTransform } from '@angular/core';
import { environment } from '../../environments/environment';
import { Idioma, idioma, t } from './i18n/i18n';

/**
 * Formato de números y montos según el idioma elegido. Es el único lugar que decide la moneda:
 * español y portugués usan la moneda configurada en el entorno (reales, como siempre) y el
 * inglés usa dólares con formato de EE. UU. ($1,234.56).
 */
interface Formato {
  moneda: Intl.NumberFormat;
  numero: Intl.NumberFormat;
  /** Separación entre el número y el signo %: "12 %" en español, "12%" en portugués e inglés. */
  espacioPorcentaje: string;
}

const CONFIG: Record<Idioma, { moneda: string; localeMoneda: string; localeNumero: string; espacioPorcentaje: string }> = {
  es: { moneda: environment.moneda, localeMoneda: environment.localeMoneda, localeNumero: 'es', espacioPorcentaje: ' ' },
  'pt-BR': { moneda: environment.moneda, localeMoneda: environment.localeMoneda, localeNumero: 'pt-BR', espacioPorcentaje: '' },
  en: { moneda: 'USD', localeMoneda: 'en-US', localeNumero: 'en-US', espacioPorcentaje: '' },
};

const cache = new Map<Idioma, Formato>();

function formato(): Formato {
  const i = idioma();
  let f = cache.get(i);
  if (!f) {
    const c = CONFIG[i];
    f = {
      moneda: new Intl.NumberFormat(c.localeMoneda, { style: 'currency', currency: c.moneda }),
      numero: new Intl.NumberFormat(c.localeNumero, { maximumFractionDigits: 2 }),
      espacioPorcentaje: c.espacioPorcentaje,
    };
    cache.set(i, f);
  }
  return f;
}

/** Formatea un valor según su tipo de línea de desglose. */
export function formatear(valor: number | null | undefined, tipo: string = 'moneda'): string {
  if (valor === null || valor === undefined || Number.isNaN(valor)) return '—';
  const f = formato();
  switch (tipo) {
    case 'moneda': return f.moneda.format(valor);
    case 'porcentaje': return `${f.numero.format(valor)}${f.espacioPorcentaje}%`;
    case 'dias': return t('{0} días', f.numero.format(valor));
    default: return f.numero.format(valor);
  }
}

/** Uso en plantillas: {{ valor | fmt }} o {{ valor | fmt: 'porcentaje' }}. Se actualiza al cambiar el idioma. */
@Pipe({ name: 'fmt', pure: false })
export class FormatoPipe implements PipeTransform {
  transform(valor: number | null | undefined, tipo: string = 'moneda'): string {
    return formatear(valor, tipo);
  }
}

/** Símbolo de la moneda del idioma actual (R$, $…). */
export function simboloMoneda(): string {
  return formato().moneda.formatToParts(0).find((p) => p.type === 'currency')?.value ?? '$';
}
