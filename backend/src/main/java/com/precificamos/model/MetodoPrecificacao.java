package com.precificamos.model;

/** Método usado para obtener el precio sugerido de un producto. */
public enum MetodoPrecificacao {
    /** Margen de ganancia deseado como % del precio de venta. */
    MARGEM,
    /** Multiplicador aplicado sobre el costo unitario. */
    MARKUP
}
