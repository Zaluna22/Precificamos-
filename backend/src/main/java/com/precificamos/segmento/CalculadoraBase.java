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
 * y el margen se expresa como % del precio de venta (no del costo). La parte de costos fijos
 * que le toca a cada unidad (rateo) se suma <b>al costo</b>: no se multiplica por el markup ni
 * se infla con el margen; solo se le agregan los impuestos y tasas sobre la venta.</p>
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
     * {@code precio = costo / (1 - tasas - margen)}. Sin costos fijos (por ejemplo, liquidación).
     */
    public static BigDecimal precioPorMargen(BigDecimal costo, BigDecimal porcentajeSobreVenta, BigDecimal margenPct) {
        return precioPorMargen(costo, BigDecimal.ZERO, porcentajeSobreVenta, margenPct);
    }

    /**
     * Precio por margen con los costos fijos sumados al costo: el margen se aplica solo al costo
     * variable y la parte de fijos se suma sin margen, con impuestos y tasas encima:
     * {@code precio = costoVariable / (1 - tasas - margen) + rateo / (1 - tasas)}.
     * Así la ganancia por unidad es {@code margen × (costoVariable / (1 - tasas - margen))}.
     */
    public static BigDecimal precioPorMargen(BigDecimal costoVariable, BigDecimal rateo, BigDecimal porcentajeSobreVenta,
                                             BigDecimal margenPct) {
        BigDecimal divisor = BigDecimal.ONE.subtract(porcentajeSobreVenta).subtract(fraccion(margenPct));
        if (divisor.signum() <= 0) {
            throw new RegraDeNegocioException(t("Margen + impuestos/tasas llegan al 100 % o más del precio"));
        }
        return costoVariable.divide(divisor, MC).add(precioMinimo(rateo, porcentajeSobreVenta));
    }

    /** Precio aplicando un multiplicador sobre el costo y agregando impuestos/tasas encima (sin costos fijos). */
    public static BigDecimal precioPorMarkup(BigDecimal costo, BigDecimal porcentajeSobreVenta, BigDecimal markup) {
        return precioPorMarkup(costo, BigDecimal.ZERO, porcentajeSobreVenta, markup);
    }

    /**
     * Precio por markup con los costos fijos sumados al costo: el multiplicador se aplica solo al
     * costo variable y después se agregan impuestos y tasas:
     * {@code precio = (costoVariable × markup + rateo) / (1 - tasas)}.
     */
    public static BigDecimal precioPorMarkup(BigDecimal costoVariable, BigDecimal rateo, BigDecimal porcentajeSobreVenta,
                                             BigDecimal markup) {
        if (markup == null || markup.signum() <= 0) throw new RegraDeNegocioException(t("El markup debe ser positivo"));
        return precioMinimo(costoVariable.multiply(markup, MC).add(rateo), porcentajeSobreVenta);
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
     * ítem que habría que vender por mes para pagar <b>su parte</b> de los costos fijos
     * ({@code rateo × ventas del producto}), la misma que descuenta la ganancia mensual: así
     * {@code ganancia mensual < 0} si y solo si las ventas no llegan al punto de equilibrio. Si no se
     * informa el total de unidades del negocio, esa parte es el total de los costos fijos.
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
            BigDecimal pe = parteFijosProducto(c).divide(contribucion, 0, RoundingMode.CEILING);
            peUnidades = pe.intValueExact();
            peFacturacion = pe.multiply(precio);
        }
        BigDecimal mensual = lucro.multiply(BigDecimal.valueOf(c.ventasMensualesProducto()));
        return new Analisis(precio, rateo, costoVariable.add(rateo), tasas, contribucion, lucro, margen,
                peUnidades, peFacturacion, mensual);
    }

    /** Costos fijos del mes que le tocan a este producto: rateo × unidades vendidas del producto. */
    public static BigDecimal parteFijosProducto(ComunesResueltos c) {
        return rateo(c).multiply(BigDecimal.valueOf(c.ventasMensualesProducto()), MC);
    }

    /**
     * Si no se informó el total de unidades del negocio, todos los costos fijos caen sobre este
     * producto. Cuando eso pesa más que el propio costo variable, conviene avisar.
     */
    public static boolean fijosConcentradosEnElProducto(ComunesResueltos c, BigDecimal costoVariable) {
        return !c.unidadesNegocioInformadas() && rateo(c).compareTo(costoVariable) > 0;
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
