package com.precificamos.segmento.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Datos de un producto comprado listo para revender (segmento {@code REVENTA}).
 *
 * @param nombreProducto              nombre del producto
 * @param costoCompra                 precio pagado al proveedor por unidad
 * @param fletePorUnidad              flete de compra prorrateado por unidad
 * @param impuestosEntradaPct         impuestos de entrada sobre el costo de compra (ICMS-ST, DIFAL, importación…)
 * @param otrosCostosUnidad           embalaje, bolsa, etiqueta por unidad vendida
 * @param markup                      multiplicador sobre el costo total (2 = precio del doble del costo, antes de tasas)
 * @param margenMinimoLiquidacionPct  margen mínimo aceptable para liquidar stock parado (% del precio)
 * @param stockActual                 unidades en stock, para calcular la rotación
 */
public record EntradaReventa(
        @NotBlank String nombreProducto,
        @NotNull @Positive BigDecimal costoCompra,
        @PositiveOrZero BigDecimal fletePorUnidad,
        @PositiveOrZero BigDecimal impuestosEntradaPct,
        @PositiveOrZero BigDecimal otrosCostosUnidad,
        @Positive BigDecimal markup,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal margenMinimoLiquidacionPct,
        @PositiveOrZero Integer stockActual) {

    public EntradaReventa {
        if (fletePorUnidad == null) fletePorUnidad = BigDecimal.ZERO;
        if (impuestosEntradaPct == null) impuestosEntradaPct = BigDecimal.ZERO;
        if (otrosCostosUnidad == null) otrosCostosUnidad = BigDecimal.ZERO;
        if (markup == null) markup = BigDecimal.valueOf(2);
        if (margenMinimoLiquidacionPct == null) margenMinimoLiquidacionPct = BigDecimal.valueOf(5);
        if (stockActual == null) stockActual = 0;
    }
}
