import { Injectable } from '@angular/core';

/** Guarda en el navegador lo que el usuario fue cargando en cada categoría, para no perderlo. */
@Injectable({ providedIn: 'root' })
export class BorradorService {
  private clave(codigo: string) { return `precificamos.borrador.${codigo}`; }

  leer<T>(codigo: string): T | null {
    try {
      const crudo = localStorage.getItem(this.clave(codigo));
      return crudo ? (JSON.parse(crudo) as T) : null;
    } catch {
      return null;
    }
  }

  guardar(codigo: string, valor: unknown): void {
    try { localStorage.setItem(this.clave(codigo), JSON.stringify(valor)); } catch { /* sin espacio o modo privado */ }
  }

  borrar(codigo: string): void {
    try { localStorage.removeItem(this.clave(codigo)); } catch { /* ignorar */ }
  }
}
