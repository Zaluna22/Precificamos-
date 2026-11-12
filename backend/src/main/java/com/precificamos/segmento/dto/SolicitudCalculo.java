package com.precificamos.segmento.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

/**
 * Cuerpo de {@code POST /api/segmentos/{segmento}/calcular} y {@code /simular}.
 *
 * @param comun  parámetros comunes (costos fijos, impuestos, tasas, ventas)
 * @param datos  datos propios del segmento; su forma depende de la estrategia elegida
 * @param precio precio a simular (solo se usa en {@code /simular})
 */
public record SolicitudCalculo(ParametrosComunes comun, JsonNode datos, BigDecimal precio) {}
