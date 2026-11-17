import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { FormatoPipe, formatear, formatearFecha, simboloMoneda } from '../../core/formato';
import { TraducirPipe, t } from '../../core/i18n/i18n';
import { DefinicionSegmento, ProductoGuardado } from '../../core/modelos';
import { NumeroDecimal } from '../../core/numero-decimal';
import { generarPdfProductos, precargarPdf } from './pdf-productos';

/** "Mis productos": lo que el usuario guardó desde la calculadora, con su precio final y descarga en PDF. */
@Component({
  selector: 'app-meus-produtos',
  imports: [FormsModule, RouterLink, NumeroDecimal, FormatoPipe, TraducirPipe],
  templateUrl: './meus-produtos.html',
})
export class MeusProdutos {
  private readonly api = inject(ApiService);

  protected readonly productos = signal<ProductoGuardado[]>([]);
  protected readonly segmentos = signal<DefinicionSegmento[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly seleccion = signal<ReadonlySet<number>>(new Set());
  protected readonly generando = signal(false);
  /** Producto cuyo precio final se está editando en la lista. */
  protected readonly editandoPrecio = signal<number | null>(null);
  protected precioEditado: number | null = null;

  protected readonly cantidadSeleccionados = computed(() => this.seleccion().size);
  protected readonly todosSeleccionados = computed(() =>
    this.productos().length > 0 && this.productos().every((p) => this.seleccion().has(p.id)));

  protected get simbolo(): string { return simboloMoneda(); }

  constructor() {
    this.api.productosGuardados().subscribe({
      next: (l) => {
        this.productos.set(l);
        this.cargando.set(false);
        if (l.length) precargarPdf();
      },
      error: (e) => { this.error.set(e.erro); this.cargando.set(false); },
    });
    // Los nombres de las categorías vienen traducidos de la lista de categorías.
    this.api.segmentos().subscribe({ next: (s) => this.segmentos.set(s), error: () => { /* se muestra el código */ } });
  }

  /** Nombre de la categoría en el idioma actual (si no está, el código). */
  protected categoria(codigo: string): string {
    const s = this.segmentos().find((x) => x.codigo === codigo);
    return s ? `${s.icono} ${s.nombre}` : codigo;
  }

  private categoriaSinIcono(codigo: string): string {
    return this.segmentos().find((x) => x.codigo === codigo)?.nombre ?? codigo;
  }

  protected fecha(p: ProductoGuardado): string {
    return formatearFecha(p.actualizadoEn ?? p.creadoEn);
  }

  protected precio(p: ProductoGuardado): number | null {
    return p.precioFinal ?? p.precioSugerido;
  }

  protected seleccionado(id: number): boolean {
    return this.seleccion().has(id);
  }

  protected alternar(id: number): void {
    const s = new Set(this.seleccion());
    if (s.has(id)) s.delete(id); else s.add(id);
    this.seleccion.set(s);
  }

  protected alternarTodos(): void {
    this.seleccion.set(this.todosSeleccionados() ? new Set() : new Set(this.productos().map((p) => p.id)));
  }

  protected empezarEdicionPrecio(p: ProductoGuardado): void {
    this.editandoPrecio.set(p.id);
    this.precioEditado = this.precio(p);
    this.error.set(null);
  }

  protected cancelarEdicionPrecio(): void {
    this.editandoPrecio.set(null);
  }

  protected guardarPrecio(p: ProductoGuardado): void {
    const valor = Number(this.precioEditado);
    if (this.precioEditado === null || String(this.precioEditado).trim() === '' || !(valor >= 0)) {
      this.error.set(t('Poné un precio final válido'));
      return;
    }
    this.api.cambiarProducto(p.id, { precioFinal: valor }).subscribe({
      next: (nuevo) => {
        this.productos.update((l) => l.map((x) => (x.id === nuevo.id ? nuevo : x)));
        this.editandoPrecio.set(null);
        this.error.set(null);
      },
      error: (e) => this.error.set(e.erro),
    });
  }

  protected borrar(p: ProductoGuardado): void {
    if (!confirm(t('¿Borrar “{0}” de tus productos?', p.nombre))) return;
    this.api.borrarProducto(p.id).subscribe({
      next: () => {
        this.productos.update((l) => l.filter((x) => x.id !== p.id));
        const s = new Set(this.seleccion());
        s.delete(p.id);
        this.seleccion.set(s);
      },
      error: (e) => this.error.set(e.erro),
    });
  }

  protected async descargarPdf(): Promise<void> {
    const elegidos = this.productos().filter((p) => this.seleccion().has(p.id));
    if (!elegidos.length) return;
    this.generando.set(true);
    this.error.set(null);
    try {
      await generarPdfProductos({
        titulo: t('Mis productos'),
        fecha: t('Fecha: {0}', formatearFecha(new Date())),
        columnas: [t('Nombre'), t('Categoría'), t('Precio final')],
        filas: elegidos.map((p) => [p.nombre, this.categoriaSinIcono(p.segmento), formatear(this.precio(p))]),
        pie: t('Generado con Precificamos'),
        archivo: `${t('mis-productos')}-${new Date().toISOString().slice(0, 10)}.pdf`,
      });
    } catch {
      this.error.set(t('No pudimos generar el PDF. Probá de nuevo.'));
    } finally {
      this.generando.set(false);
    }
  }
}
