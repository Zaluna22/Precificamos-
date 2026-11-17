package com.precificamos.segmento;

import java.math.BigDecimal;

/**
 * Parámetros comunes ya completados (sin nulos) que reciben las estrategias.
 *
 * @param costosFijosMensuales     total de costos fijos del mes
 * @param porcentajeSobreVenta     impuestos + tarjeta + marketplace + comisión, como fracción (0,15 = 15 %)
 * @param ventasMensualesProducto  unidades de este ítem vendidas por mes
 * @param unidadesMensualesNegocio unidades totales del negocio por mes (base del prorrateo de fijos)
 * @param valorHora                valor de la hora de trabajo guardado en la configuración
 * @param unidadesNegocioInformadas si el total de unidades del negocio vino en la solicitud o en la
 *                                 configuración; si es {@code false} se usó {@code ventasMensualesProducto}
 *                                 y todos los costos fijos caen sobre este producto
 */
public record ComunesResueltos(BigDecimal costosFijosMensuales, BigDecimal porcentajeSobreVenta,
                               int ventasMensualesProducto, int unidadesMensualesNegocio, BigDecimal valorHora,
                               boolean unidadesNegocioInformadas) {

    /** Con el total de unidades del negocio informado. */
    public ComunesResueltos(BigDecimal costosFijosMensuales, BigDecimal porcentajeSobreVenta,
                            int ventasMensualesProducto, int unidadesMensualesNegocio, BigDecimal valorHora) {
        this(costosFijosMensuales, porcentajeSobreVenta, ventasMensualesProducto, unidadesMensualesNegocio, valorHora, true);
    }
}
