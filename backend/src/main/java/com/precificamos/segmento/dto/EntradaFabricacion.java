package com.precificamos.segmento.dto;

import com.precificamos.segmento.UnidadMedida;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos de una pieza fabricada para el segmento {@code FABRICACION}.
 *
 * @param nombreProducto      nombre de la pieza (ej.: "Remera estampada talle M")
 * @param materiasPrimas      telas, maderas, aceites, resinas… con su unidad de medida y consumo por pieza
 * @param insumos             avíos e insumos que se cuentan por unidad: botones, cierres, etiquetas, frascos
 * @param minutosManoObra     minutos de trabajo por pieza
 * @param costoPorMinuto      costo de un minuto de mano de obra; si es nulo se usa el valor hora de la configuración ÷ 60
 * @param mermaPct            desperdicio de materia prima (recortes, fallas)
 * @param margenMinoristaPct  margen deseado vendiendo al consumidor final (% del precio)
 * @param margenMayoristaPct  margen deseado vendiendo por mayor a revendedores (% del precio)
 */
public record EntradaFabricacion(
        @NotBlank String nombreProducto,
        @Valid List<MateriaPrima> materiasPrimas,
        @Valid List<Insumo> insumos,
        @PositiveOrZero BigDecimal minutosManoObra,
        @PositiveOrZero BigDecimal costoPorMinuto,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal mermaPct,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal margenMinoristaPct,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal margenMayoristaPct) {

    public EntradaFabricacion {
        if (materiasPrimas == null) materiasPrimas = List.of();
        if (insumos == null) insumos = List.of();
        if (minutosManoObra == null) minutosManoObra = BigDecimal.ZERO;
        if (mermaPct == null) mermaPct = BigDecimal.ZERO;
        if (margenMinoristaPct == null) margenMinoristaPct = BigDecimal.valueOf(40);
        if (margenMayoristaPct == null) margenMayoristaPct = BigDecimal.valueOf(20);
    }

    /**
     * Materia prima comprada en una unidad y consumida en otra (ej.: tela comprada por metro,
     * usada en centímetros).
     */
    public record MateriaPrima(
            @NotBlank String nombre,
            @NotNull @PositiveOrZero BigDecimal precioCompra,
            @NotNull @Positive BigDecimal cantidadCompra,
            @NotNull UnidadMedida unidadCompra,
            @NotNull @PositiveOrZero BigDecimal consumoPorPieza,
            UnidadMedida unidadConsumo) {
        public MateriaPrima {
            if (unidadConsumo == null) unidadConsumo = unidadCompra;
        }
    }

    /** Insumo o avío que se cuenta por unidad. */
    public record Insumo(
            @NotBlank String nombre,
            @NotNull @PositiveOrZero BigDecimal costoUnitario,
            @NotNull @PositiveOrZero BigDecimal cantidadPorPieza) {}
}
