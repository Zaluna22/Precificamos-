package com.precificamos.dto;

import java.math.BigDecimal;

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
