package com.precificamos.segmento.dto;

import java.math.BigDecimal;

/**
 * Una línea del desglose de costos o un indicador extra del resultado.
 *
 * @param clave    identificador estable para el frontend
 * @param concepto texto para mostrar
 * @param valor    valor numérico (redondeado a 2 decimales)
 * @param tipo     {@code moneda}, {@code porcentaje}, {@code numero} o {@code dias}
 */
public record LineaDesglose(String clave, String concepto, BigDecimal valor, String tipo) {
    public static LineaDesglose moneda(String clave, String concepto, BigDecimal valor) {
        return new LineaDesglose(clave, concepto, valor, "moneda");
    }
    public static LineaDesglose porcentaje(String clave, String concepto, BigDecimal valor) {
        return new LineaDesglose(clave, concepto, valor, "porcentaje");
    }
    public static LineaDesglose numero(String clave, String concepto, BigDecimal valor) {
        return new LineaDesglose(clave, concepto, valor, "numero");
    }
}
