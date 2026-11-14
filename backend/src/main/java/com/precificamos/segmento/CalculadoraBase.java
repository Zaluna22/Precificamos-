package com.precificamos.segmento;

import com.precificamos.service.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static com.precificamos.i18n.Textos.t;

/**
 * Fórmulas comunes a todos los segmentos: prorrateo de costos fijos, precio por margen o por
 * markup, y análisis de un precio (ganancia, margen y punto de equilibrio).
 *
 * <p>Convenciones: los porcentajes sobre la venta (impuestos y tasas) se descuentan del precio,
 * y el margen se expresa como % del precio de venta (no del costo).</p>
 */
public final class CalculadoraBase {
    public static final MathContext MC = MathContext.DECIMAL64;
    public static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private CalculadoraBase() {}

    /** Convierte un porcentaje (15) en fracción (0,15); nulo cuenta como cero. */
    public static BigDecimal fraccion(BigDecimal pct) {
        return pct == null ? BigDecimal.ZERO : pct.divide(CIEN, MC);
    }

    /** Valor o cero si es nulo. */
    public static BigDecimal cero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Parte de los costos fijos mensuales que le corresponde a cada unidad vendida. */
    public static BigDecimal rateo(ComunesResueltos c) {
        return c.costosFijosMensuales().divide(BigDecimal.valueOf(c.unidadesMensualesNegocio()), MC);
    }

    /**
     * Precio para que, después de impuestos y tasas, quede el margen pedido sobre el precio:
     * {@code precio = costo / (1 - tasas - margen)}.
     */
    public static BigDecimal precioPorMargen(BigDecimal costo, BigDecimal porcentajeSobreVenta, BigDecimal margenPct) {
        BigDecimal divisor = BigDecimal.ONE.subtract(porcentajeSobreVenta).subtract(fraccion(margenPct));
        if (divisor.signum() <= 0) {
            throw new RegraDeNegocioException(t("Margen + impuestos/tasas llegan al 100 % o más del precio"));
        }
        return costo.divide(divisor, MC);
    }

    /** Precio aplicando un multiplicador sobre el costo y agregando impuestos/tasas encima. */
    public static BigDecimal precioPorMarkup(BigDecimal costo, BigDecimal porcentajeSobreVenta, BigDecimal markup) {
        if (markup == null || markup.signum() <= 0) throw new RegraDeNegocioException(t("El markup debe ser positivo"));
        return precioMinimo(costo, porcentajeSobreVenta).multiply(markup, MC);
    }

    /** Precio que solo cubre el costo y las tasas (ganancia cero). */
    public static BigDecimal precioMinimo(BigDecimal costo, BigDecimal porcentajeSobreVenta) {
        BigDecimal liquido = BigDecimal.ONE.subtract(porcentajeSobreVenta);
        if (liquido.signum() <= 0) throw new RegraDeNegocioException(t("Impuestos/tasas llegan al 100 % o más"));
        return costo.divide(liquido, MC);
    }

    /** Resultado de analizar un precio de venta. */
    public record Analisis(BigDecimal precio, BigDecimal rateo, BigDecimal costoTotal, BigDecimal tasas,
                           BigDecimal contribucion, BigDecimal lucro, BigDecimal margenPct,
                           Integer puntoEquilibrioUnidades, BigDecimal puntoEquilibrioFacturacion,
                           BigDecimal lucroMensual) {}

    /**
     * Analiza un precio: tasas, contribución (lo que queda para pagar fijos), ganancia neta,
     * margen y punto de equilibrio. El punto de equilibrio es la cantidad de unidades de este
     * ítem que habría que vender por mes para pagar todos los costos fijos.
     */
    public static Analisis analizar(BigDecimal precio, BigDecimal costoVariable, ComunesResueltos c) {
        if (precio == null || precio.signum() <= 0) throw new RegraDeNegocioException(t("El precio debe ser positivo"));
        BigDecimal rateo = rateo(c);
        BigDecimal tasas = precio.multiply(c.porcentajeSobreVenta(), MC);
        BigDecimal contribucion = precio.subtract(tasas).subtract(costoVariable);
        BigDecimal lucro = contribucion.subtract(rateo);
        BigDecimal margen = lucro.divide(precio, MC).multiply(CIEN);
        Integer peUnidades = null;
        BigDecimal peFacturacion = null;
        if (contribucion.signum() > 0) {
            BigDecimal pe = c.costosFijosMensuales().divide(contribucion, 0, RoundingMode.CEILING);
            peUnidades = pe.intValueExact();
            peFacturacion = pe.multiply(precio);
        }
        BigDecimal mensual = lucro.multiply(BigDecimal.valueOf(c.ventasMensualesProducto()));
        return new Analisis(precio, rateo, costoVariable.add(rateo), tasas, contribucion, lucro, margen,
                peUnidades, peFacturacion, mensual);
    }

    /** Redondeo para mostrar: 2 decimales, mitad hacia arriba. */
    public static BigDecimal r(BigDecimal v) {
        return v == null ? null : v.setScale(2, RoundingMode.HALF_UP);
    }

    /** Valida que un porcentaje de pérdida/merma esté entre 0 y menos de 100 y devuelve el factor 1/(1-p). */
    public static BigDecimal factorMerma(BigDecimal pct, String concepto) {
        BigDecimal f = fraccion(pct);
        if (f.signum() < 0 || f.compareTo(BigDecimal.ONE) >= 0) {
            throw new RegraDeNegocioException(t("{0} debe estar entre 0 y 99,99 %", t(concepto)));
        }
        return BigDecimal.ONE.divide(BigDecimal.ONE.subtract(f), MC);
    }
}
