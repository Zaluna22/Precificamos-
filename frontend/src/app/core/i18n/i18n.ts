import { Injectable, Pipe, PipeTransform, signal } from '@angular/core';
import { EN } from './en';
import { PT_BR } from './pt-br';

/** Idiomas de la app. El español es el idioma original de todos los textos. */
export type Idioma = 'es' | 'pt-BR' | 'en';

export interface OpcionIdioma {
  codigo: Idioma;
  /** Nombre en su propio idioma, para el selector. */
  nombre: string;
  /** Abreviatura para el encabezado. */
  corto: string;
}

export const IDIOMAS: readonly OpcionIdioma[] = [
  { codigo: 'es', nombre: 'Español', corto: 'ES' },
  { codigo: 'pt-BR', nombre: 'Português (Brasil)', corto: 'PT' },
  { codigo: 'en', nombre: 'English', corto: 'EN' },
];

const CLAVE = 'precificamos.idioma';

/** Traducciones: la clave es el texto en español. Lo que falta se muestra en español. */
const DICCIONARIOS: Record<Idioma, Record<string, string>> = { es: {}, 'pt-BR': PT_BR, en: EN };

/** Interpreta una etiqueta de idioma ("pt", "pt-BR", "en-US"…); null si no es soportada. */
export function idiomaDesde(etiqueta: string | null | undefined): Idioma | null {
  const e = (etiqueta ?? '').trim().toLowerCase();
  if (e === 'pt' || e.startsWith('pt-')) return 'pt-BR';
  if (e === 'en' || e.startsWith('en-')) return 'en';
  if (e === 'es' || e.startsWith('es-')) return 'es';
  return null;
}

/** Idioma inicial: el guardado; si no hay, el del navegador (pt → PT-BR, en → EN, el resto → ES). */
function idiomaInicial(): Idioma {
  try {
    const guardado = idiomaDesde(localStorage.getItem(CLAVE));
    if (guardado) return guardado;
  } catch { /* modo privado */ }
  if (typeof navigator !== 'undefined') {
    const preferidos = navigator.languages?.length ? navigator.languages : [navigator.language];
    const primero = idiomaDesde(preferidos[0]);
    if (primero) return primero;
  }
  return 'es';
}

const actual = signal<Idioma>(idiomaInicial());
aplicarAlDocumento(actual());

/** Idioma elegido (signal de solo lectura: las plantillas se actualizan solas al cambiarlo). */
export const idioma = actual.asReadonly();

/** Cambia el idioma al instante, sin recargar, y lo recuerda en este navegador. */
export function cambiarIdioma(nuevo: Idioma): void {
  if (nuevo === actual()) return;
  actual.set(nuevo);
  try { localStorage.setItem(CLAVE, nuevo); } catch { /* sin espacio o modo privado */ }
  aplicarAlDocumento(nuevo);
}

/**
 * Traduce un texto escrito en español al idioma actual. Los datos variables van como
 * {0}, {1}…: t('Paso {0} de {1}', 2, 5).
 */
export function t(es: string, ...args: unknown[]): string {
  let texto = DICCIONARIOS[actual()][es] ?? es;
  args.forEach((a, i) => { texto = texto.split(`{${i}}`).join(String(a)); });
  return texto;
}

function aplicarAlDocumento(i: Idioma): void {
  if (typeof document !== 'undefined') document.documentElement.lang = i;
}

/** Uso en plantillas: {{ 'Costos fijos' | t }} o {{ 'Paso {0} de {1}' | t: n: total }}. */
@Pipe({ name: 't', pure: false })
export class TraducirPipe implements PipeTransform {
  transform(es: string | null | undefined, ...args: unknown[]): string {
    return es ? t(es, ...args) : '';
  }
}

/** Acceso inyectable al idioma (para componentes que prefieren no importar funciones sueltas). */
@Injectable({ providedIn: 'root' })
export class I18nService {
  readonly idioma = idioma;
  readonly idiomas = IDIOMAS;
  cambiar(i: Idioma): void { cambiarIdioma(i); }
  t(es: string, ...args: unknown[]): string { return t(es, ...args); }
}

/** Diccionarios, exportados para las verificaciones de completitud. */
export const DICCIONARIOS_TRADUCCION = DICCIONARIOS;
