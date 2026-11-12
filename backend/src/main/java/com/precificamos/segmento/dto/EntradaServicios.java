package com.precificamos.segmento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos de un servicio profesional o técnico (segmento {@code SERVICIOS}).
 *
 * @param nombreServicio            nombre del servicio (ej.: "Corte y brushing", "Instalación de toma")
 * @param horasPorServicio          horas que lleva hacerlo (incluida la preparación)
 * @param valorHora                 cuánto vale tu hora; si es nulo se usa la configuración
 * @param materiales                materiales que se gastan en cada servicio
 * @param kmDesplazamiento          kilómetros de ida y vuelta
 * @param costoPorKm                combustible + desgaste por km
 * @param otrosGastosDesplazamiento peajes, estacionamiento, pasajes
 * @param margenPct                 margen deseado sobre el precio
 */
public record EntradaServicios(
        @NotBlank String nombreServicio,
        @NotNull @Positive BigDecimal horasPorServicio,
        @PositiveOrZero BigDecimal valorHora,
        @Valid List<Material> materiales,
        @PositiveOrZero BigDecimal kmDesplazamiento,
        @PositiveOrZero BigDecimal costoPorKm,
        @PositiveOrZero BigDecimal otrosGastosDesplazamiento,
        @PositiveOrZero @DecimalMax(value = "100", inclusive = false) BigDecimal margenPct) {

    public EntradaServicios {
        if (materiales == null) materiales = List.of();
        if (kmDesplazamiento == null) kmDesplazamiento = BigDecimal.ZERO;
        if (costoPorKm == null) costoPorKm = BigDecimal.ZERO;
        if (otrosGastosDesplazamiento == null) otrosGastosDesplazamiento = BigDecimal.ZERO;
        if (margenPct == null) margenPct = BigDecimal.valueOf(30);
    }

    /** Material consumido en el servicio. */
    public record Material(@NotBlank String nombre, @NotNull @PositiveOrZero BigDecimal costo) {}
}
