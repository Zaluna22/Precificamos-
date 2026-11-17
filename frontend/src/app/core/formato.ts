import { Pipe, PipeTransform } from '@angular/core';
import { environment } from '../../environments/environment';
import { Idioma, idioma, t } from './i18n/i18n';

/**
 * Formato de números y montos según el idioma elegido. Es el único lugar que decide la moneda:
 * portugués usa la moneda configurada en el entorno (reales, R$ 1.234,56), español muestra solo
 * el signo $ con formato rioplatense ($ 1.234,56) y el inglés usa dólares de EE. UU. ($1,234.56).
 */
interface Formato {
  moneda: Intl.NumberFormat;
  numero: Intl.NumberFormat;
  /** Separación entre el número y el signo %: "12 %" en español, "12%" en portugués e inglés. */
  espacioPorcentaje: string;
}

const CONFIG: Record<Idioma, { moneda: string; localeMoneda: string; localeNumero: string; espacioPorcentaje: string }> = {
  // Español: solo el signo $ (narrowSymbol), sin "R$" ni "US$".
  es: { moneda: 'USD', localeMoneda: 'es-AR', localeNumero: 'es', espacioPorcentaje: ' ' },
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
      moneda: new Intl.NumberFormat(c.localeMoneda, { style: 'currency', currency: c.moneda, currencyDisplay: 'narrowSymbol' }),
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

const LOCALE_FECHA: Record<Idioma, string> = { es: 'es-AR', 'pt-BR': 'pt-BR', en: 'en-US' };

/** Fecha corta en el formato del idioma actual (17/11/2026 o 11/17/2026). */
export function formatearFecha(fecha: string | Date | null | undefined): string {
  if (!fecha) return '—';
  const d = typeof fecha === 'string' ? new Date(fecha) : fecha;
  if (Number.isNaN(d.getTime())) return '—';
  return new Intl.DateTimeFormat(LOCALE_FECHA[idioma()], { day: '2-digit', month: '2-digit', year: 'numeric' }).format(d);
}
