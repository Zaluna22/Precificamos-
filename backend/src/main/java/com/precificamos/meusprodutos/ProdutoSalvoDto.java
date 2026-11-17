package com.precificamos.meusprodutos;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;

/** Producto guardado tal como lo devuelve la API (sin el id del dueño). */
public record ProdutoSalvoDto(
        Long id,
        String segmento,
        String nombre,
        JsonNode comun,
        JsonNode datos,
        BigDecimal precioSugerido,
        BigDecimal costoTotalUnitario,
        BigDecimal margenRealPct,
        BigDecimal lucroUnitario,
        BigDecimal precioFinal,
        Instant creadoEn,
        Instant actualizadoEn) {}
