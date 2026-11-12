package com.precificamos.segmento.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Datos de un artículo de supermercado/almacén/autoservicio (segmento {@code VENTA_MASIVA}).
 *
 * @param nombreProducto        nombre del artículo
 * @param categoria             categoría de góndola (ALMACEN, BEBIDAS, LACTEOS, …) o una propia
 * @param costoCompra           costo de compra por unidad
 * @param perdidaPct            pérdida por vencimiento, rotura o robo (% de lo comprado)
 * @param margenPct             margen para este artículo; si es nulo se usa el de su categoría
 * @param margenesPorCategoria  márgenes propios por categoría que reemplazan a los predeterminados
 * @param redondeoPsicologico   si es verdadero, el precio se sube al siguiente ,99
 * @param promoLleva            promoción "lleve N…" (ej.: 3)
 * @param promoPaga             "…pague M" (ej.: 2)
 */
public record EntradaVentaMasiva(
        @NotBlank String nombreProducto,
        String categoria,
        @NotNull @Positive BigDecimal costoCompra,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal perdidaPct,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal margenPct,
        Map<String, BigDecimal> margenesPorCategoria,
        Boolean redondeoPsicologico,
        @Positive Integer promoLleva,
        @Positive Integer promoPaga) {

    public EntradaVentaMasiva {
        if (categoria == null || categoria.isBlank()) categoria = "OTROS";
        if (perdidaPct == null) perdidaPct = BigDecimal.ZERO;
        if (margenesPorCategoria == null) margenesPorCategoria = Map.of();
        if (redondeoPsicologico == null) redondeoPsicologico = Boolean.TRUE;
    }
}
