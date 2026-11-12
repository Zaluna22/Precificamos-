// Tipos que reflejan los DTO del backend (paquete com.precificamos.segmento.dto).

export type TipoCampo =
  | 'texto' | 'moneda' | 'porcentaje' | 'numero' | 'entero' | 'booleano' | 'seleccion' | 'lista';

export interface Opcion {
  valor: string;
  etiqueta: string;
  extra?: unknown;
}

export interface CampoFormulario {
  nombre: string;
  etiqueta: string;
  tipo: TipoCampo;
  grupo: 'datos' | 'comun';
  obligatorio: boolean;
  ayuda?: string;
  valorPorDefecto?: unknown;
  opciones?: Opcion[];
  subcampos?: CampoFormulario[];
}

export interface PasoFormulario {
  titulo: string;
  descripcion: string;
  campos: CampoFormulario[];
}

export interface DefinicionSegmento {
  codigo: string;
  nombre: string;
  descripcion: string;
  icono: string;
  ejemplos: string[];
  pasos: PasoFormulario[];
}

export interface LineaDesglose {
  clave: string;
  concepto: string;
  valor: number;
  tipo: 'moneda' | 'porcentaje' | 'numero' | 'dias';
}

export interface ResultadoSegmento {
  segmento: string;
  nombre: string;
  costoVariableUnitario: number;
  rateoCostosFijos: number;
  costoTotalUnitario: number;
  precioSugerido: number;
  metodo: string;
  impuestosYTasasUnitario: number;
  lucroUnitario: number;
  margenRealPct: number;
  puntoEquilibrioUnidades: number | null;
  puntoEquilibrioFacturacion: number | null;
  lucroMensualEstimado: number;
  desglose: LineaDesglose[];
  extras: LineaDesglose[];
  avisos: string[];
}

export interface ResultadoSimulacion {
  precio: number;
  costoTotalUnitario: number;
  impuestosYTasasUnitario: number;
  contribucionUnitaria: number;
  lucroUnitario: number;
  margenPct: number;
  puntoEquilibrioUnidades: number | null;
  ventasMensuales: number;
  facturacionMensual: number;
  lucroMensual: number;
  cubreCostos: boolean;
}

/** Cuerpo de /calcular y /simular. */
export interface SolicitudCalculo {
  comun: Record<string, unknown>;
  datos: Record<string, unknown>;
  precio?: number;
}

export interface CostoFijo {
  id?: number;
  nome: string;
  valorMensal: number;
}

/** Configuración general (nombres en portugués porque así los expone /api/configuracao). */
export interface Configuracion {
  valorHora: number;
  impostoPct: number;
  taxaCartaoPct: number;
  taxaMarketplacePct: number;
  comissaoPct: number;
  totalUnidadesMes: number;
}

/** Error devuelto por la API: mensaje general y, si corresponde, error por campo. */
export interface ErrorApi {
  erro: string;
  campos?: Record<string, string>;
}
