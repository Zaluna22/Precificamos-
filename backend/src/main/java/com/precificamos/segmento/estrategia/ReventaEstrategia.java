package com.precificamos.segmento.estrategia;

import com.precificamos.i18n.Textos;
import com.precificamos.segmento.*;
import com.precificamos.segmento.dto.CampoFormulario;
import com.precificamos.segmento.dto.EntradaReventa;
import com.precificamos.segmento.dto.LineaDesglose;
import com.precificamos.segmento.dto.PasoFormulario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.precificamos.segmento.CalculadoraBase.*;
import static com.precificamos.i18n.Textos.t;

/**
 * Reventa / comercio minorista de productos comprados listos.
 *
 * <p>Costo puesto en el local = compra × (1 + impuestos de entrada) + flete + otros. El precio
 * sugerido sale del markup; además se calcula un precio de liquidación (sin cargar costos
 * fijos, que se pagan igual) que respeta el margen mínimo, y la rotación del stock.</p>
 */
@Component
public class ReventaEstrategia implements EstrategiaPrecificacion<EntradaReventa> {

    /** A partir de cuántos días de stock se sugiere liquidar. */
    static final int DIAS_STOCK_ALERTA = 90;

    @Override public Segmento segmento() { return Segmento.REVENTA; }

    @Override public Class<EntradaReventa> tipoEntrada() { return EntradaReventa.class; }

    @Override
    public List<PasoFormulario> pasos() {
        return List.of(
                new PasoFormulario("El producto", "¿Qué revendés y cuánto te cuesta?", List.of(
                        CampoFormulario.de("nombreProducto", "Nombre del producto", "texto", true,
                                "Ej.: Funda de celular, Perfume 100 ml", null),
                        CampoFormulario.de("costoCompra", "Precio de compra por unidad", "moneda", true,
                                "Lo que le pagás al proveedor por cada unidad", null))),
                new PasoFormulario("Costos de entrada", "Lo que pagás hasta tener el producto en tu local.", List.of(
                        CampoFormulario.de("fletePorUnidad", "Flete por unidad", "moneda", false,
                                "Flete total ÷ unidades compradas", 0),
                        CampoFormulario.de("impuestosEntradaPct", "Impuestos de entrada (%)", "porcentaje", false,
                                "ICMS-ST, DIFAL o impuestos de importación sobre el costo", 0),
                        CampoFormulario.de("otrosCostosUnidad", "Embalaje u otros por unidad", "moneda", false,
                                "Bolsa, caja, etiqueta", 0))),
                new PasoFormulario("Precio y stock", "Cuánto querés multiplicar tu costo y cómo rota tu stock.", List.of(
                        CampoFormulario.de("markup", "Markup (multiplicador)", "numero", false,
                                "2 = vender al doble del costo. En ropa se usa 2 a 2,5; en electrónica 1,3 a 1,6", 2),
                        CampoFormulario.de("margenMinimoLiquidacionPct", "Margen mínimo para liquidar (%)", "porcentaje", false,
                                "Para ofertas de stock parado sin perder plata", 5),
                        CampoFormulario.de("stockActual", "Unidades en stock", "entero", false,
                                "Para saber cuántos días tardás en venderlas", 0))));
    }

    @Override
    public CostoVariable costoVariable(EntradaReventa e, ComunesResueltos c) {
        BigDecimal impuestos = e.costoCompra().multiply(fraccion(e.impuestosEntradaPct()), MC);
        BigDecimal total = e.costoCompra().add(impuestos).add(e.fletePorUnidad()).add(e.otrosCostosUnidad());
        return new CostoVariable(e.nombreProducto(), total, List.of(
                LineaDesglose.moneda("compra", "Precio de compra", r(e.costoCompra())),
                LineaDesglose.moneda("impuestosEntrada", "Impuestos de entrada", r(impuestos)),
                LineaDesglose.moneda("flete", "Flete", r(e.fletePorUnidad())),
                LineaDesglose.moneda("otros", "Embalaje y otros", r(e.otrosCostosUnidad()))));
    }

    @Override
    public PropuestaPrecio proponerPrecio(EntradaReventa e, CostoVariable costo, ComunesResueltos c) {
        BigDecimal precio = precioPorMarkup(costo.total().add(rateo(c)), c.porcentajeSobreVenta(), e.markup());
        // En una liquidación los costos fijos ya están pagos: solo se exige cubrir el costo variable y el margen mínimo.
        BigDecimal liquidacion = precioPorMargen(costo.total(), c.porcentajeSobreVenta(), e.margenMinimoLiquidacionPct());

        PropuestaPrecio p = new PropuestaPrecio(precio,
                t("Markup de {0}× sobre el costo total, más impuestos y tasas", Textos.numero(e.markup())));
        p.extras().add(LineaDesglose.moneda("precioLiquidacion", "Precio mínimo de liquidación", r(liquidacion)));
        p.extras().add(LineaDesglose.porcentaje("descuentoMaximoPct", "Descuento máximo posible",
                r(BigDecimal.ONE.subtract(liquidacion.divide(precio, MC)).multiply(CIEN))));
        if (e.stockActual() > 0) {
            BigDecimal dias = BigDecimal.valueOf(e.stockActual()).multiply(BigDecimal.valueOf(30))
                    .divide(BigDecimal.valueOf(c.ventasMensualesProducto()), 0, RoundingMode.CEILING);
            BigDecimal capital = costo.total().multiply(BigDecimal.valueOf(e.stockActual()));
            p.extras().add(new LineaDesglose("diasStock", "Días de stock (rotación)", dias, "dias"));
            p.extras().add(LineaDesglose.moneda("capitalInmovilizado", "Capital inmovilizado en stock", r(capital)));
            if (dias.intValue() > DIAS_STOCK_ALERTA) {
                p.avisos().add(t("Tenés stock para {0} días. Considerá una promoción o liquidación hasta {1} para liberar capital.",
                        dias, Textos.moneda(r(liquidacion))));
            }
        }
        if (e.markup().compareTo(BigDecimal.ONE) <= 0) {
            p.avisos().add(t("Con un markup de 1 o menos vendés al costo o perdés plata."));
        }
        return p;
    }
}
