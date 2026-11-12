package com.precificamos.dto;

import java.math.BigDecimal;

public record ResultadoSimulacao(BigDecimal preco, BigDecimal custoUnitario, BigDecimal lucroUnitario,
                                 BigDecimal margemPct, BigDecimal lucroMensal) {}
