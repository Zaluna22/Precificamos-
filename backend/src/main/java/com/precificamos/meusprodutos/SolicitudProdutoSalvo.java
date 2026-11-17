package com.precificamos.meusprodutos;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Cuerpo de {@code POST /api/meus-produtos} y {@code PUT /api/meus-produtos/{id}}.
 *
 * @param segmento     código de la categoría usada en la calculadora
 * @param nombre       nombre del producto
 * @param comun        parámetros comunes tal cual se mandaron a /calcular
 * @param datos        datos de la categoría tal cual se mandaron a /calcular
 * @param precioFinal  precio elegido por el usuario; si es nulo se usa {@code precioSugerido}
 */
public record SolicitudProdutoSalvo(
        @NotBlank(message = "Indicá la categoría") String segmento,
        @NotBlank(message = "Poné un nombre al producto")
        @Size(max = 120, message = "El nombre del producto puede tener hasta 120 caracteres")
        String nombre,
        JsonNode comun,
        JsonNode datos,
        BigDecimal precioSugerido,
        BigDecimal costoTotalUnitario,
        BigDecimal margenRealPct,
        BigDecimal lucroUnitario,
        @PositiveOrZero(message = "El precio final no puede ser negativo") BigDecimal precioFinal) {}
