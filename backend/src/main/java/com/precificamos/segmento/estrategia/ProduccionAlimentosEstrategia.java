package com.precificamos.segmento.estrategia;

import com.precificamos.i18n.Textos;
import com.precificamos.segmento.*;
import com.precificamos.segmento.dto.CampoFormulario;
import com.precificamos.segmento.dto.EntradaProduccionAlimentos;
import com.precificamos.segmento.dto.EntradaProduccionAlimentos.Ingrediente;
import com.precificamos.segmento.dto.LineaDesglose;
import com.precificamos.segmento.dto.PasoFormulario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.precificamos.segmento.CalculadoraBase.*;
import static com.precificamos.i18n.Textos.t;

/**
 * Producción de alimentos y bebidas elaborados (gastronomía).
 *
 * <p>Costo por porción = (ingredientes × factor de merma + energía) / rendimiento + embalaje.
 * El precio sale del CMV objetivo: {@code precio = mercadería por porción / CMV%}. Si ese precio
 * no alcanza a cubrir el costo total más impuestos y tasas, se sube al precio mínimo y se avisa.</p>
 */
@Component
public class ProduccionAlimentosEstrategia implements EstrategiaPrecificacion<EntradaProduccionAlimentos> {

    @Override public Segmento segmento() { return Segmento.PRODUCCION_ALIMENTOS; }

    @Override public Class<EntradaProduccionAlimentos> tipoEntrada() { return EntradaProduccionAlimentos.class; }

    @Override
    public List<PasoFormulario> pasos() {
        return List.of(
                new PasoFormulario("Tu receta", "Contanos qué preparás y cuántas porciones salen.", List.of(
                        CampoFormulario.de("nombreReceta", "Nombre de la receta o producto", "texto", true,
                                "Ej.: Torta de chocolate, Hamburguesa clásica", null),
                        CampoFormulario.de("rendimientoPorciones", "¿Cuántas porciones o unidades rinde?", "entero", true,
                                "Ej.: una torta de 12 porciones → 12", 12))),
                new PasoFormulario("Ingredientes", "Cargá cada ingrediente: cuánto pagaste por el paquete y cuánto usás.", List.of(
                        CampoFormulario.lista("ingredientes", "Ingredientes", true,
                                "Ej.: Harina, pagué 6,50 por 1 kg y uso 500 g", List.of(
                                        CampoFormulario.de("nombre", "Ingrediente", "texto", true, null, null),
                                        CampoFormulario.de("precioCompra", "Precio del paquete", "moneda", true, null, null),
                                        CampoFormulario.de("cantidadCompra", "Tamaño del paquete", "numero", true, null, 1),
                                        unidad("unidadCompra", "Unidad del paquete", "KG"),
                                        CampoFormulario.de("cantidadUsada", "Cantidad usada", "numero", true, null, null),
                                        unidad("unidadUsada", "Unidad usada", "G"))))),
                new PasoFormulario("Elaboración", "Pérdidas, gas/luz y embalaje.", List.of(
                        CampoFormulario.de("mermaPct", "Merma o desperdicio (%)", "porcentaje", false,
                                "Lo que se pierde al pelar, cortar o cocinar. Suele ser 5–10 %", 5),
                        CampoFormulario.de("minutosPreparacion", "Minutos de horno/hornalla", "numero", false,
                                "Tiempo total de equipos encendidos para toda la receta", 0),
                        CampoFormulario.de("costoEnergiaHora", "Costo de gas y luz por hora", "moneda", false,
                                "Un horno doméstico a gas ronda 2 a 4 por hora", 0),
                        CampoFormulario.de("embalajePorPorcion", "Embalaje por porción", "moneda", false,
                                "Caja, bandeja, etiqueta, servilleta", 0))),
                new PasoFormulario("Objetivo de precio", "El CMV es cuánto del precio se va en ingredientes.", List.of(
                        CampoFormulario.de("cmvObjetivoPct", "CMV objetivo (%)", "porcentaje", false,
                                "En gastronomía se usa 25–35 %. Más bajo = precio más alto", 30))));
    }

    private static CampoFormulario unidad(String nombre, String etiqueta, String porDefecto) {
        return CampoFormulario.seleccion(nombre, etiqueta, true, null, porDefecto, Unidades.opciones());
    }

    @Override
    public CostoVariable costoVariable(EntradaProduccionAlimentos e, ComunesResueltos c) {
        BigDecimal ingredientes = costoIngredientes(e);
        BigDecimal porciones = BigDecimal.valueOf(e.rendimientoPorciones());
        BigDecimal conMerma = ingredientes.multiply(factorMerma(e.mermaPct(), "La merma"), MC);
        BigDecimal energia = e.minutosPreparacion().multiply(e.costoEnergiaHora()).divide(BigDecimal.valueOf(60), MC);

        BigDecimal ingredientesPorcion = ingredientes.divide(porciones, MC);
        BigDecimal mermaPorcion = conMerma.subtract(ingredientes).divide(porciones, MC);
        BigDecimal energiaPorcion = energia.divide(porciones, MC);
        BigDecimal total = ingredientesPorcion.add(mermaPorcion).add(energiaPorcion).add(e.embalajePorPorcion());

        List<LineaDesglose> desglose = new ArrayList<>();
        desglose.add(LineaDesglose.moneda("ingredientes", "Ingredientes por porción", r(ingredientesPorcion)));
        desglose.add(LineaDesglose.moneda("merma", "Merma por porción", r(mermaPorcion)));
        desglose.add(LineaDesglose.moneda("energia", "Gas y luz por porción", r(energiaPorcion)));
        desglose.add(LineaDesglose.moneda("embalaje", "Embalaje por porción", r(e.embalajePorPorcion())));
        return new CostoVariable(e.nombreReceta(), total, desglose);
    }

    @Override
    public PropuestaPrecio proponerPrecio(EntradaProduccionAlimentos e, CostoVariable costo, ComunesResueltos c) {
        BigDecimal porciones = BigDecimal.valueOf(e.rendimientoPorciones());
        // Mercadería = ingredientes con merma (lo que cuenta para el CMV).
        BigDecimal mercaderia = costoIngredientes(e).multiply(factorMerma(e.mermaPct(), "La merma"), MC).divide(porciones, MC);

        BigDecimal precioCmv = mercaderia.divide(fraccion(e.cmvObjetivoPct()), MC);
        BigDecimal minimo = precioMinimo(costo.total().add(rateo(c)), c.porcentajeSobreVenta());

        PropuestaPrecio p;
        if (precioCmv.compareTo(minimo) >= 0) {
            p = new PropuestaPrecio(precioCmv,
                    t("CMV objetivo de {0} %: mercadería por porción ÷ CMV", Textos.numero(e.cmvObjetivoPct())));
        } else {
            p = new PropuestaPrecio(minimo, t("Precio mínimo que cubre costos e impuestos (el CMV objetivo no alcanzaba)"));
            p.avisos().add(t("Con un CMV de {0} % no cubrís tus costos fijos y tasas. Bajá el CMV objetivo o revisá tus costos fijos.",
                    Textos.numero(e.cmvObjetivoPct())));
        }
        p.extras().add(LineaDesglose.moneda("costoReceta", "Costo total de la receta",
                r(costo.total().multiply(porciones))));
        p.extras().add(LineaDesglose.moneda("precioPorCmv", "Precio según CMV objetivo", r(precioCmv)));
        p.extras().add(LineaDesglose.moneda("precioMinimo", "Precio mínimo (ganancia cero)", r(minimo)));
        p.extras().add(LineaDesglose.porcentaje("cmvRealPct", "CMV real al precio sugerido",
                r(mercaderia.divide(p.precio(), MC).multiply(CIEN))));
        return p;
    }

    /** Suma del costo de los ingredientes usados en toda la receta (sin merma). */
    private static BigDecimal costoIngredientes(EntradaProduccionAlimentos e) {
        BigDecimal total = BigDecimal.ZERO;
        for (Ingrediente i : e.ingredientes()) {
            total = total.add(i.unidadCompra().costoProporcional(
                    i.precioCompra(), i.cantidadCompra(), i.cantidadUsada(), i.unidadUsada(), i.nombre()));
        }
        return total;
    }
}
