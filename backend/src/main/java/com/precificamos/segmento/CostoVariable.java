package com.precificamos.segmento;

import com.precificamos.segmento.dto.LineaDesglose;

import java.math.BigDecimal;
import java.util.List;

/**
 * Costo variable de una unidad (pieza, porción o servicio) calculado por una estrategia.
 *
 * @param nombre   nombre del producto, receta o servicio
 * @param total    costo variable unitario sin redondear
 * @param desglose líneas que lo componen (ya redondeadas, solo para mostrar)
 */
public record CostoVariable(String nombre, BigDecimal total, List<LineaDesglose> desglose) {}
