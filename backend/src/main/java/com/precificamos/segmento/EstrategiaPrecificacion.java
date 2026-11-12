package com.precificamos.segmento;

import com.precificamos.segmento.dto.PasoFormulario;

import java.util.List;

/**
 * Estrategia de precificación de un segmento (patrón Strategy). Cada implementación define
 * sus propios datos de entrada, cómo se calcula el costo variable de una unidad y cómo se
 * llega al precio sugerido. Lo común (costos fijos, impuestos, tasas, punto de equilibrio y
 * simulador) lo resuelve {@link SegmentoService} con {@link CalculadoraBase}.
 *
 * @param <T> tipo del DTO de entrada del segmento
 */
public interface EstrategiaPrecificacion<T> {

    /** Segmento que atiende esta estrategia. */
    Segmento segmento();

    /** Clase del DTO de entrada, usada para convertir el JSON de {@code datos}. */
    Class<T> tipoEntrada();

    /** Pasos del formulario propios del segmento (sin el paso común). */
    List<PasoFormulario> pasos();

    /** Costo variable de una unidad (sin costos fijos ni tasas sobre la venta). */
    CostoVariable costoVariable(T entrada, ComunesResueltos comunes);

    /** Precio sugerido a partir del costo variable y de los parámetros comunes. */
    PropuestaPrecio proponerPrecio(T entrada, CostoVariable costo, ComunesResueltos comunes);
}
