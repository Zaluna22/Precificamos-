import { Component, input } from '@angular/core';
import { FormatoPipe } from '../core/formato';
import { ResultadoSegmento } from '../core/modelos';

/** Tarjeta con el precio sugerido, costos, margen, punto de equilibrio, desglose y avisos. */
@Component({
  selector: 'app-tarjeta-resultado',
  imports: [FormatoPipe],
  templateUrl: './tarjeta-resultado.html',
})
export class TarjetaResultado {
  readonly resultado = input.required<ResultadoSegmento>();
}
