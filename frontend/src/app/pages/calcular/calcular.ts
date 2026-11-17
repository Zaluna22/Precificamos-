import { Component, computed, effect, inject, input, OnInit, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { BorradorService } from '../../core/borrador.service';
import { CampoFormulario, DefinicionSegmento, ProductoGuardado, ResultadoSegmento, SolicitudCalculo } from '../../core/modelos';
import { CampoDinamico, filaVacia } from '../../componentes/campo-dinamico';
import { TarjetaResultado } from '../../componentes/tarjeta-resultado';
import { Simulador } from '../../componentes/simulador';
import { PistaSesion } from '../../componentes/pista-sesion';
import { CLAVE_RECALCULAR, GuardarProducto } from '../../componentes/guardar-producto';
import { AuthService } from '../../core/auth.service';
import { TraducirPipe, idioma, t } from '../../core/i18n/i18n';

interface Modelo {
  datos: Record<string, unknown>;
  comun: Record<string, unknown>;
}

const vacio = (v: unknown) => v === null || v === undefined || (typeof v === 'string' && v.trim() === '');

/**
 * Formulario guiado de una categoría: un paso por pantalla, validación simple en cada paso,
 * resultado con desglose y simulador de precio.
 */
@Component({
  selector: 'app-calcular',
  imports: [FormsModule, RouterLink, CampoDinamico, TarjetaResultado, Simulador, PistaSesion, GuardarProducto, TraducirPipe],
  templateUrl: './calcular.html',
})
export class Calcular implements OnInit {
  private readonly api = inject(ApiService);
  private readonly borradores = inject(BorradorService);
  private readonly auth = inject(AuthService);

  /** Código de la categoría, tomado de la ruta /calcular/:codigo. */
  readonly codigo = input.required<string>();
  /** Id de un producto de "Mis productos" para editarlo (/calcular/:codigo?producto=12). */
  readonly producto = input<string | undefined>();

  protected readonly definicion = signal<DefinicionSegmento | null>(null);
  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly errorGeneral = signal<string | null>(null);
  protected readonly errores = signal<Record<string, string>>({});
  protected readonly paso = signal(0);
  protected readonly resultado = signal<ResultadoSegmento | null>(null);
  protected readonly solicitudEnviada = signal<SolicitudCalculo | null>(null);
  protected modelo: Modelo = { datos: {}, comun: {} };
  /** Producto guardado que se está editando (o el que se acaba de guardar): guardar de nuevo lo actualiza. */
  protected readonly productoGuardado = signal<ProductoGuardado | null>(null);
  /** Precio que el usuario eligió en el simulador (o el precio final guardado, al editar). */
  protected readonly precioElegido = signal<number | null>(null);

  protected readonly totalPasos = computed(() => this.definicion()?.pasos.length ?? 0);
  protected readonly pasoActual = computed(() => this.definicion()?.pasos[this.paso()] ?? null);
  protected readonly esUltimo = computed(() => this.paso() === this.totalPasos() - 1);
  protected readonly progreso = computed(() =>
    this.totalPasos() ? Math.round(((this.paso() + 1) / this.totalPasos()) * 100) : 0);

  constructor() {
    // Al cambiar el idioma se piden de nuevo los textos del formulario (vienen de la API) sin
    // perder lo cargado, y si ya había un resultado se recalcula para traer el desglose traducido.
    let primera = true;
    effect(() => {
      idioma();
      if (primera) { primera = false; return; }
      untracked(() => this.recargarPorIdioma());
    });
  }

  ngOnInit(): void {
    this.api.segmentos().subscribe({
      next: (lista) => {
        const def = this.buscar(lista);
        if (!def) {
          this.errorGeneral.set(t('Esa categoría no existe. Volvé al inicio y elegí una.'));
        } else {
          this.definicion.set(def);
          const id = Number(this.producto());
          if (id > 0 && this.auth.logueado()) {
            this.cargarGuardado(def, id);
            return;
          }
          this.modelo = this.borradores.leer<Modelo>(def.codigo) ?? this.modeloInicial(def);
          if (this.debeRecalcular(def)) this.calcular();
        }
        this.cargando.set(false);
      },
      error: (e) => { this.errorGeneral.set(e.erro); this.cargando.set(false); },
    });
  }

  /** Abre un producto de "Mis productos": completa el formulario con lo que se guardó. */
  private cargarGuardado(def: DefinicionSegmento, id: number): void {
    this.api.productoGuardado(id).subscribe({
      next: (p) => {
        const m = this.modeloInicial(def);
        Object.assign(m.comun, p.comun ?? {});
        Object.assign(m.datos, p.datos ?? {});
        this.modelo = m;
        this.productoGuardado.set(p);
        this.precioElegido.set(p.precioFinal);
        this.cargando.set(false);
      },
      error: (e) => {
        // Si no se encuentra, se sigue con un cálculo nuevo y se avisa.
        this.modelo = this.borradores.leer<Modelo>(def.codigo) ?? this.modeloInicial(def);
        this.errorGeneral.set(e.erro);
        this.cargando.set(false);
      },
    });
  }

  /** Volvió de crear la cuenta o entrar para guardar: recalcula con el borrador para mostrar el resultado. */
  private debeRecalcular(def: DefinicionSegmento): boolean {
    try {
      const pendiente = sessionStorage.getItem(CLAVE_RECALCULAR);
      if (pendiente === null) return false;
      sessionStorage.removeItem(CLAVE_RECALCULAR);
      return pendiente === def.codigo && this.auth.logueado() && this.borradores.leer(def.codigo) !== null;
    } catch {
      return false;
    }
  }

  protected alGuardar(p: ProductoGuardado): void {
    this.productoGuardado.set(p);
  }

  private buscar(lista: DefinicionSegmento[]): DefinicionSegmento | undefined {
    return lista.find((s) => s.codigo.toLowerCase() === this.codigo().toLowerCase().replace(/-/g, '_'));
  }

  private recargarPorIdioma(): void {
    if (!this.definicion()) return;
    this.errores.set({});
    this.errorGeneral.set(null);
    this.api.segmentos().subscribe({
      next: (lista) => {
        const def = this.buscar(lista);
        if (def) this.definicion.set(def);
        if (this.resultado()) this.calcular();
      },
      error: (e) => this.errorGeneral.set(e.erro),
    });
  }

  /** Modelo con los valores por defecto de cada campo; las listas arrancan con una fila. */
  private modeloInicial(def: DefinicionSegmento): Modelo {
    const m: Modelo = { datos: {}, comun: {} };
    for (const p of def.pasos) {
      for (const c of p.campos) {
        const destino = c.grupo === 'comun' ? m.comun : m.datos;
        destino[c.nombre] = c.tipo === 'lista' ? [filaVacia(c)] : (c.valorPorDefecto ?? null);
      }
    }
    return m;
  }

  protected destino(c: CampoFormulario): Record<string, unknown> {
    return c.grupo === 'comun' ? this.modelo.comun : this.modelo.datos;
  }

  protected siguiente(): void {
    if (!this.validarPaso()) return;
    this.limpiarAlerta();
    this.guardarBorrador();
    if (this.esUltimo()) {
      this.calcular();
    } else {
      this.paso.update((p) => p + 1);
      this.subir();
    }
  }

  protected anterior(): void {
    this.limpiarAlerta();
    this.paso.update((p) => Math.max(0, p - 1));
    this.subir();
  }

  protected irAPaso(i: number): void {
    if (i < this.paso() || this.validarPaso()) {
      this.limpiarAlerta();
      this.paso.set(i);
      this.subir();
    }
  }

  protected editar(): void {
    this.limpiarAlerta();
    this.resultado.set(null);
    this.paso.set(0);
    this.subir();
  }

  protected empezarDeNuevo(): void {
    const def = this.definicion();
    if (!def) return;
    this.borradores.borrar(def.codigo);
    this.modelo = this.modeloInicial(def);
    this.productoGuardado.set(null);
    this.precioElegido.set(null);
    this.errores.set({});
    this.errorGeneral.set(null);
    this.editar();
  }

  /** Revisa los obligatorios del paso actual, incluidas las columnas de cada fila. */
  private validarPaso(): boolean {
    const paso = this.pasoActual();
    if (!paso) return false;
    const errores: Record<string, string> = {};
    for (const c of paso.campos) {
      const clave = `${c.grupo}.${c.nombre}`;
      const valor = this.destino(c)[c.nombre];
      if (c.tipo === 'lista') {
        const filas = (valor as Record<string, unknown>[]) ?? [];
        if (c.obligatorio && filas.length === 0) errores[clave] = t('Agregá al menos uno');
        filas.forEach((fila, i) => {
          for (const s of c.subcampos ?? []) {
            if (s.obligatorio && vacio(fila[s.nombre])) errores[`${clave}[${i}].${s.nombre}`] = t('Completá este dato');
          }
        });
      } else if (c.obligatorio && vacio(valor)) {
        errores[clave] = t('Completá este dato');
      } else if (typeof valor === 'number' && valor < 0) {
        errores[clave] = t('No puede ser negativo');
      }
    }
    this.errores.set(errores);
    return Object.keys(errores).length === 0;
  }

  /** Arma la solicitud sin valores vacíos (el backend completa con la configuración guardada). */
  private solicitud(): SolicitudCalculo {
    const limpiar = (o: Record<string, unknown>): Record<string, unknown> => {
      const r: Record<string, unknown> = {};
      for (const [k, v] of Object.entries(o)) {
        if (vacio(v)) continue;
        r[k] = Array.isArray(v) ? v.map((f) => limpiar(f as Record<string, unknown>)) : v;
      }
      return r;
    };
    return { datos: limpiar(this.modelo.datos), comun: limpiar(this.modelo.comun) };
  }

  private calcular(): void {
    const def = this.definicion();
    if (!def) return;
    const solicitud = this.solicitud();
    this.enviando.set(true);
    this.errorGeneral.set(null);
    this.api.calcular(def.codigo, solicitud).subscribe({
      next: (r) => {
        this.resultado.set(r);
        this.errores.set({});
        this.solicitudEnviada.set(solicitud);
        this.enviando.set(false);
        this.subir();
      },
      error: (e) => {
        this.enviando.set(false);
        this.errorGeneral.set(e.erro);
        if (e.campos) {
          this.errores.set(e.campos);
          // Lleva al usuario al primer paso con error.
          const primero = Object.keys(e.campos)[0];
          const idx = def.pasos.findIndex((p) => p.campos.some((c) => primero?.startsWith(`${c.grupo}.${c.nombre}`)));
          if (idx >= 0) this.paso.set(idx);
        }
      },
    });
  }

  protected ventasDelModelo(): number {
    return Number(this.modelo.comun['ventasMensualesProducto']) || 1;
  }

  /**
   * Saca el aviso general del formulario (por ejemplo "Revisá los datos ingresados") al cambiar de
   * paso: ya cumplió su función de llevar al usuario al campo con error y, si sigue mal, el próximo
   * cálculo lo vuelve a mostrar.
   */
  private limpiarAlerta(): void {
    if (this.definicion()) this.errorGeneral.set(null);
  }

  private guardarBorrador(): void {
    const def = this.definicion();
    // Al editar un producto guardado no se pisa el borrador de la categoría.
    if (def && !this.productoGuardado()) this.borradores.guardar(def.codigo, this.modelo);
  }

  private subir(): void {
    if (typeof window !== 'undefined') window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
