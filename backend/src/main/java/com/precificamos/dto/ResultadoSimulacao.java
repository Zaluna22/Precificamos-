package com.precificamos.dto;

import java.math.BigDecimal;

/** Resultado de simular la venta de un producto guardado a un precio dado. */
public record ResultadoSimulacao(BigDecimal preco, BigDecimal custoUnitario, BigDecimal lucroUnitario,
                                 BigDecimal margemPct, BigDecimal lucroMensal) {}
