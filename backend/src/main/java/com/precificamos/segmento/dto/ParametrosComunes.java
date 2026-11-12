package com.precificamos.segmento.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Datos comunes a todos los segmentos. Cualquier campo nulo se completa con la configuración
 * guardada del negocio ({@code /api/configuracao}) y con la suma de los costos fijos
 * ({@code /api/custos-fixos}).
 *
 * @param costosFijosMensuales     alquiler, servicios, contador, DAS del MEI, etc.
 * @param impuestoPct              alícuota sobre la venta (Simples Nacional, por ejemplo). MEI paga un
 *                                 valor fijo mensual: en ese caso dejar 0 y sumar el DAS a costos fijos.
 * @param tarjetaPct               tasa de la tarjeta o del medio de pago
 * @param marketplacePct           comisión del marketplace o de la app de delivery
 * @param comisionPct              comisión de vendedores o revendedores
 * @param ventasMensualesProducto  unidades (piezas, porciones o servicios) de este ítem vendidas por mes
 * @param unidadesMensualesNegocio total de unidades que vende el negocio por mes, para prorratear los
 *                                 costos fijos. Si es nulo se usa la configuración o, en su defecto,
 *                                 {@code ventasMensualesProducto}.
 */
public record ParametrosComunes(
        @PositiveOrZero BigDecimal costosFijosMensuales,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal impuestoPct,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal tarjetaPct,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal marketplacePct,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal comisionPct,
        @Positive Integer ventasMensualesProducto,
        @Positive Integer unidadesMensualesNegocio) {

    /** Instancia sin datos: todo se toma de la configuración guardada. */
    public static ParametrosComunes vacios() {
        return new ParametrosComunes(null, null, null, null, null, null, null);
    }
}
