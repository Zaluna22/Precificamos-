package com.precificamos.segmento.dto;

import java.util.List;

/**
 * Lo que devuelve {@code GET /api/segmentos}: la categoría, negocios de ejemplo para que el
 * usuario reconozca el suyo y los pasos del formulario (el último siempre es el común).
 */
public record DefinicionSegmento(String codigo, String nombre, String descripcion, String icono,
                                 List<String> ejemplos, List<PasoFormulario> pasos) {}
