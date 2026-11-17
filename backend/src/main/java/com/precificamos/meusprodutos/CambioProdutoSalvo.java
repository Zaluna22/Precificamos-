package com.precificamos.meusprodutos;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Cuerpo de {@code PATCH /api/meus-produtos/{id}}: cambia solo lo que viene (nombre o precio final). */
public record CambioProdutoSalvo(
        @Size(max = 120, message = "El nombre del producto puede tener hasta 120 caracteres") String nombre,
        @PositiveOrZero(message = "El precio final no puede ser negativo") BigDecimal precioFinal) {}
