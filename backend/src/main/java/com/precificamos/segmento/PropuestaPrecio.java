package com.precificamos.segmento;

import com.precificamos.segmento.dto.LineaDesglose;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Precio que propone una estrategia, con la explicación del método, indicadores extra y avisos.
 */
public record PropuestaPrecio(BigDecimal precio, String metodo, List<LineaDesglose> extras, List<String> avisos) {
    public PropuestaPrecio(BigDecimal precio, String metodo) {
        this(precio, metodo, new ArrayList<>(), new ArrayList<>());
    }
}
