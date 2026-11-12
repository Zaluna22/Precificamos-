package com.precificamos.segmento.dto;

import java.math.BigDecimal;

/**
 * Qué pasa si se vende a un precio elegido por el usuario.
 *
 * @param contribucionUnitaria lo que queda de cada venta para pagar los costos fijos
 *                             (precio neto de tasas - costo variable)
 */
public record ResultadoSimulacionSegmento(
        BigDecimal precio,
        BigDecimal costoTotalUnitario,
        BigDecimal impuestosYTasasUnitario,
        BigDecimal contribucionUnitaria,
        BigDecimal lucroUnitario,
        BigDecimal margenPct,
        Integer puntoEquilibrioUnidades,
        Integer ventasMensuales,
        BigDecimal facturacionMensual,
        BigDecimal lucroMensual,
        boolean cubreCostos) {}
