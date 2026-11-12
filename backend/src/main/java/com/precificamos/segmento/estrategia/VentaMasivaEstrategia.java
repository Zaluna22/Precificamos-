package com.precificamos.segmento.estrategia;

import com.precificamos.segmento.*;
import com.precificamos.segmento.dto.CampoFormulario;
import com.precificamos.segmento.dto.CampoFormulario.Opcion;
import com.precificamos.segmento.dto.EntradaVentaMasiva;
import com.precificamos.segmento.dto.LineaDesglose;
import com.precificamos.segmento.dto.PasoFormulario;
import com.precificamos.service.RegraDeNegocioException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.precificamos.segmento.CalculadoraBase.*;

/**
 * Venta masiva multi-categoría de alto giro (supermercado, almacén, autoservicio).
 *
 * <p>Costo = compra ÷ (1 − pérdida). El margen depende de la categoría de góndola (configurable),
 * el precio se puede redondear a ,99 y se analiza la promoción "lleve N pague M".</p>
 */
@Component
public class VentaMasivaEstrategia implements EstrategiaPrecificacion<EntradaVentaMasiva> {

    /** Márgenes de referencia por categoría (% sobre el precio). Son un punto de partida, no una regla. */
    public static final Map<String, BigDecimal> MARGENES_POR_DEFECTO;
    private static final Map<String, String> NOMBRES;

    static {
        Map<String, BigDecimal> m = new LinkedHashMap<>();
        Map<String, String> n = new LinkedHashMap<>();
        agregar(m, n, "ALMACEN", "Almacén (secos)", 22);
        agregar(m, n, "BEBIDAS", "Bebidas", 25);
        agregar(m, n, "LACTEOS", "Lácteos y fiambres", 20);
        agregar(m, n, "CARNES", "Carnicería", 28);
        agregar(m, n, "FRUTAS_VERDURAS", "Frutas y verduras", 35);
        agregar(m, n, "PANADERIA", "Panadería", 40);
        agregar(m, n, "CONGELADOS", "Congelados", 25);
        agregar(m, n, "LIMPIEZA", "Limpieza", 30);
        agregar(m, n, "PERFUMERIA", "Perfumería e higiene", 32);
        agregar(m, n, "BAZAR", "Bazar y hogar", 40);
        agregar(m, n, "OTROS", "Otros", 25);
        MARGENES_POR_DEFECTO = Map.copyOf(m);
        NOMBRES = n;
    }

    private static void agregar(Map<String, BigDecimal> m, Map<String, String> n, String clave, String nombre, int margen) {
        m.put(clave, BigDecimal.valueOf(margen));
        n.put(clave, nombre);
    }

    @Override public Segmento segmento() { return Segmento.VENTA_MASIVA; }

    @Override public Class<EntradaVentaMasiva> tipoEntrada() { return EntradaVentaMasiva.class; }

    @Override
    public List<PasoFormulario> pasos() {
        List<Opcion> categorias = NOMBRES.entrySet().stream()
                .map(en -> new Opcion(en.getKey(), en.getValue(), MARGENES_POR_DEFECTO.get(en.getKey())))
                .toList();
        return List.of(
                new PasoFormulario("El artículo", "¿Qué artículo y en qué góndola?", List.of(
                        CampoFormulario.de("nombreProducto", "Nombre del artículo", "texto", true,
                                "Ej.: Arroz 1 kg, Gaseosa 2 l", null),
                        CampoFormulario.seleccion("categoria", "Categoría", true,
                                "Cada categoría trae un margen de referencia que podés cambiar", "ALMACEN", categorias),
                        CampoFormulario.de("costoCompra", "Costo de compra por unidad", "moneda", true, null, null))),
                new PasoFormulario("Pérdidas y margen", "Vencimientos, roturas y el margen de la categoría.", List.of(
                        CampoFormulario.de("perdidaPct", "Pérdida por vencimiento/rotura (%)", "porcentaje", false,
                                "Perecederos 5–10 %, secos 1–2 %", 2),
                        CampoFormulario.de("margenPct", "Margen para este artículo (%)", "porcentaje", false,
                                "Vacío = margen de referencia de la categoría", null))),
                new PasoFormulario("Precio en góndola y promoción", "Redondeo y ofertas.", List.of(
                        CampoFormulario.de("redondeoPsicologico", "Redondear a ,99", "booleano", false,
                                "Ej.: 4,32 → 4,99", true),
                        CampoFormulario.de("promoLleva", "Promo: lleve", "entero", false, "Ej.: lleve 3…", null),
                        CampoFormulario.de("promoPaga", "Promo: pague", "entero", false, "…pague 2", null))));
    }

    @Override
    public CostoVariable costoVariable(EntradaVentaMasiva e, ComunesResueltos c) {
        BigDecimal conPerdida = e.costoCompra().multiply(factorMerma(e.perdidaPct(), "La pérdida"), MC);
        return new CostoVariable(e.nombreProducto(), conPerdida, List.of(
                LineaDesglose.moneda("compra", "Costo de compra", r(e.costoCompra())),
                LineaDesglose.moneda("perdida", "Pérdida por vencimiento/rotura", r(conPerdida.subtract(e.costoCompra())))));
    }

    @Override
    public PropuestaPrecio proponerPrecio(EntradaVentaMasiva e, CostoVariable costo, ComunesResueltos c) {
        BigDecimal margen = margenDe(e);
        BigDecimal base = precioPorMargen(costo.total().add(rateo(c)), c.porcentajeSobreVenta(), margen);
        BigDecimal precio = Boolean.TRUE.equals(e.redondeoPsicologico()) ? redondearA99(base) : base;
        String categoria = NOMBRES.getOrDefault(e.categoria().toUpperCase(), e.categoria());

        PropuestaPrecio p = new PropuestaPrecio(precio, "Margen de " + margen.stripTrailingZeros().toPlainString()
                + " % para la categoría " + categoria
                + (Boolean.TRUE.equals(e.redondeoPsicologico()) ? ", redondeado a ,99" : ""));
        p.extras().add(LineaDesglose.porcentaje("margenCategoriaPct", "Margen de la categoría", r(margen)));
        p.extras().add(LineaDesglose.moneda("precioSinRedondeo", "Precio sin redondeo", r(base)));

        if (e.promoLleva() != null || e.promoPaga() != null) {
            analizarPromo(e, costo, c, precio, p);
        }
        return p;
    }

    private void analizarPromo(EntradaVentaMasiva e, CostoVariable costo, ComunesResueltos c,
                               BigDecimal precio, PropuestaPrecio p) {
        if (e.promoLleva() == null || e.promoPaga() == null || e.promoPaga() >= e.promoLleva()) {
            throw new RegraDeNegocioException("En la promoción 'lleve N pague M', M tiene que ser menor que N");
        }
        BigDecimal efectivo = precio.multiply(BigDecimal.valueOf(e.promoPaga()))
                .divide(BigDecimal.valueOf(e.promoLleva()), MC);
        CalculadoraBase.Analisis a = analizar(efectivo, costo.total(), c);
        p.extras().add(LineaDesglose.moneda("precioUnitarioPromo",
                "Precio por unidad en promo " + e.promoLleva() + "x" + e.promoPaga(), r(efectivo)));
        p.extras().add(LineaDesglose.porcentaje("margenPromoPct", "Margen en la promoción", r(a.margenPct())));
        p.extras().add(LineaDesglose.moneda("lucroPromoUnidad", "Ganancia por unidad en la promoción", r(a.lucro())));
        if (a.lucro().signum() < 0) {
            p.avisos().add("La promoción " + e.promoLleva() + "x" + e.promoPaga()
                    + " da pérdida. Probá una promo más chica o negociá el costo con el proveedor.");
        }
    }

    /** Margen del artículo: el propio, el configurado para su categoría o el de referencia. */
    BigDecimal margenDe(EntradaVentaMasiva e) {
        if (e.margenPct() != null) return e.margenPct();
        String cat = e.categoria().toUpperCase();
        for (Map.Entry<String, BigDecimal> en : e.margenesPorCategoria().entrySet()) {
            if (en.getKey().equalsIgnoreCase(cat)) return en.getValue();
        }
        return MARGENES_POR_DEFECTO.getOrDefault(cat, MARGENES_POR_DEFECTO.get("OTROS"));
    }

    /** Sube el precio al siguiente valor terminado en ,99 (4,32 → 4,99; 4,99 → 4,99; 5,00 → 5,99). */
    static BigDecimal redondearA99(BigDecimal precio) {
        BigDecimal centavos = precio.setScale(2, RoundingMode.CEILING);
        BigDecimal candidato = centavos.setScale(0, RoundingMode.FLOOR).add(new BigDecimal("0.99"));
        return candidato.compareTo(centavos) >= 0 ? candidato : candidato.add(BigDecimal.ONE);
    }
}
