package com.precificamos.segmento.dto;

import com.precificamos.segmento.UnidadMedida;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos de una receta / ficha técnica para el segmento {@code PRODUCCION_ALIMENTOS}.
 *
 * @param nombreReceta        nombre del plato o producto (ej.: "Torta de chocolate")
 * @param ingredientes        ingredientes con lo que se pagó y lo que se usa en la receta
 * @param rendimientoPorciones cuántas porciones/unidades salen de la receta
 * @param mermaPct            pérdida de ingredientes en la elaboración (cáscaras, recortes, evaporación)
 * @param minutosPreparacion  minutos de horno/hornalla/equipos encendidos para toda la receta
 * @param costoEnergiaHora    costo de gas y luz por hora de esos equipos
 * @param embalajePorPorcion  caja, bandeja, servilleta, etiqueta por porción
 * @param cmvObjetivoPct      costo de mercadería vendida objetivo como % del precio (gastronomía suele usar 25–35 %)
 */
public record EntradaProduccionAlimentos(
        @NotBlank String nombreReceta,
        @NotEmpty @Valid List<Ingrediente> ingredientes,
        @NotNull @Positive Integer rendimientoPorciones,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal mermaPct,
        @PositiveOrZero BigDecimal minutosPreparacion,
        @PositiveOrZero BigDecimal costoEnergiaHora,
        @PositiveOrZero BigDecimal embalajePorPorcion,
        @Positive @DecimalMax("100") BigDecimal cmvObjetivoPct) {

    /** Valores por defecto para los campos opcionales. */
    public EntradaProduccionAlimentos {
        if (mermaPct == null) mermaPct = BigDecimal.ZERO;
        if (minutosPreparacion == null) minutosPreparacion = BigDecimal.ZERO;
        if (costoEnergiaHora == null) costoEnergiaHora = BigDecimal.ZERO;
        if (embalajePorPorcion == null) embalajePorPorcion = BigDecimal.ZERO;
        if (cmvObjetivoPct == null) cmvObjetivoPct = BigDecimal.valueOf(30);
    }

    /**
     * Ingrediente de la receta.
     *
     * @param precioCompra   lo que se pagó por el paquete (ej.: 25,90)
     * @param cantidadCompra tamaño del paquete (ej.: 1)
     * @param unidadCompra   unidad del paquete (ej.: kg)
     * @param cantidadUsada  cuánto se usa en la receta (ej.: 300)
     * @param unidadUsada    unidad de lo usado (ej.: g). Si es nula, igual a la de compra.
     */
    public record Ingrediente(
            @NotBlank String nombre,
            @NotNull @PositiveOrZero BigDecimal precioCompra,
            @NotNull @Positive BigDecimal cantidadCompra,
            @NotNull UnidadMedida unidadCompra,
            @NotNull @PositiveOrZero BigDecimal cantidadUsada,
            UnidadMedida unidadUsada) {
        public Ingrediente {
            if (unidadUsada == null) unidadUsada = unidadCompra;
        }
    }
}
