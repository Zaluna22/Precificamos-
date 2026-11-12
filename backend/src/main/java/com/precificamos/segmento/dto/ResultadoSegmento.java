package com.precificamos.segmento.dto;

import com.precificamos.segmento.Segmento;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado del cálculo de precio para un segmento.
 *
 * @param costoVariableUnitario     lo que cuesta producir/comprar/prestar una unidad
 * @param rateoCostosFijos          parte de los costos fijos que le toca a cada unidad
 * @param costoTotalUnitario        costo variable + prorrateo de fijos
 * @param precioSugerido            precio de venta recomendado
 * @param metodo                    explicación corta de cómo se llegó al precio
 * @param impuestosYTasasUnitario   impuestos + tarjeta + marketplace + comisión sobre el precio sugerido
 * @param lucroUnitario             ganancia neta por unidad al precio sugerido
 * @param margenRealPct             ganancia neta / precio
 * @param puntoEquilibrioUnidades   unidades por mes para cubrir los costos fijos (nulo si no se cubren nunca)
 * @param puntoEquilibrioFacturacion facturación mensual necesaria para cubrir los costos fijos
 * @param lucroMensualEstimado      ganancia mensual con las ventas previstas del ítem
 * @param desglose                  composición del costo unitario
 * @param extras                    indicadores propios del segmento (precio mayorista, CMV, promo, etc.)
 * @param avisos                    alertas y recomendaciones en lenguaje simple
 */
public record ResultadoSegmento(
        Segmento segmento,
        String nombre,
        BigDecimal costoVariableUnitario,
        BigDecimal rateoCostosFijos,
        BigDecimal costoTotalUnitario,
        BigDecimal precioSugerido,
        String metodo,
        BigDecimal impuestosYTasasUnitario,
        BigDecimal lucroUnitario,
        BigDecimal margenRealPct,
        Integer puntoEquilibrioUnidades,
        BigDecimal puntoEquilibrioFacturacion,
        BigDecimal lucroMensualEstimado,
        List<LineaDesglose> desglose,
        List<LineaDesglose> extras,
        List<String> avisos) {}
