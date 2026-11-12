package com.precificamos.dto;

import java.math.BigDecimal;

/** Resultado del cálculo de precio de un producto guardado. */
public record ResultadoPreco(
        BigDecimal custoMateriais,
        BigDecimal custoVariavel,
        BigDecimal maoDeObra,
        BigDecimal rateioFixo,
        BigDecimal custoUnitario,
        BigDecimal precoSugerido,
        BigDecimal lucroUnitario,
        BigDecimal margemRealPct,
        BigDecimal pontoEquilibrioUnidades) {}
