package com.precificamos.segmento.estrategia;

import com.precificamos.i18n.Textos;
import com.precificamos.segmento.*;
import com.precificamos.segmento.dto.CampoFormulario;
import com.precificamos.segmento.dto.EntradaFabricacion;
import com.precificamos.segmento.dto.EntradaFabricacion.Insumo;
import com.precificamos.segmento.dto.EntradaFabricacion.MateriaPrima;
import com.precificamos.segmento.dto.LineaDesglose;
import com.precificamos.segmento.dto.PasoFormulario;
import com.precificamos.service.RegraDeNegocioException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.precificamos.segmento.CalculadoraBase.*;
import static com.precificamos.i18n.Textos.t;

/**
 * Fabricación o confección propia (ropa, artesanías, cosméticos, muebles…).
 *
 * <p>Costo por pieza = materias primas × factor de merma + insumos + minutos × costo por minuto.
 * Se calculan dos precios por margen: minorista (sugerido) y mayorista (extra).</p>
 */
@Component
public class FabricacionEstrategia implements EstrategiaPrecificacion<EntradaFabricacion> {

    @Override public Segmento segmento() { return Segmento.FABRICACION; }

    @Override public Class<EntradaFabricacion> tipoEntrada() { return EntradaFabricacion.class; }

    @Override
    public List<PasoFormulario> pasos() {
        return List.of(
                new PasoFormulario("Tu pieza", "¿Qué fabricás?", List.of(
                        CampoFormulario.de("nombreProducto", "Nombre del producto", "texto", true,
                                "Ej.: Remera estampada, Jabón de lavanda, Mesa ratona", null))),
                new PasoFormulario("Materias primas", "Lo que se mide: tela, madera, cera, aceites, hilo…", List.of(
                        CampoFormulario.lista("materiasPrimas", "Materias primas", false,
                                "Ej.: Tela, pagué 30 por 1 m y uso 80 cm por pieza", List.of(
                                        CampoFormulario.de("nombre", "Materia prima", "texto", true, null, null),
                                        CampoFormulario.de("precioCompra", "Precio pagado", "moneda", true, null, null),
                                        CampoFormulario.de("cantidadCompra", "Cantidad comprada", "numero", true, null, 1),
                                        unidad("unidadCompra", "Unidad de compra", "M"),
                                        CampoFormulario.de("consumoPorPieza", "Consumo por pieza", "numero", true, null, null),
                                        unidad("unidadConsumo", "Unidad de consumo", "CM"))))),
                new PasoFormulario("Insumos y avíos", "Lo que se cuenta por unidad: botones, cierres, etiquetas, frascos.", List.of(
                        CampoFormulario.lista("insumos", "Insumos", false, "Ej.: Botón, 0,20 cada uno, 4 por pieza", List.of(
                                CampoFormulario.de("nombre", "Insumo", "texto", true, null, null),
                                CampoFormulario.de("costoUnitario", "Costo por unidad", "moneda", true, null, null),
                                CampoFormulario.de("cantidadPorPieza", "Cantidad por pieza", "numero", true, null, 1))))),
                new PasoFormulario("Mano de obra y merma", "Tu tiempo también se cobra.", List.of(
                        CampoFormulario.de("minutosManoObra", "Minutos de trabajo por pieza", "numero", false,
                                "Ej.: 45 minutos para coser una remera", 0),
                        CampoFormulario.de("costoPorMinuto", "Costo por minuto de mano de obra", "moneda", false,
                                "Vacío = tu valor hora de Configuración ÷ 60", null),
                        CampoFormulario.de("mermaPct", "Merma de materia prima (%)", "porcentaje", false,
                                "Recortes y piezas falladas. Suele ser 5–15 %", 5))),
                new PasoFormulario("Márgenes", "Precio para el consumidor final y para revendedores.", List.of(
                        CampoFormulario.de("margenMinoristaPct", "Margen minorista (%)", "porcentaje", false,
                                "Ganancia sobre el precio al consumidor final", 40),
                        CampoFormulario.de("margenMayoristaPct", "Margen mayorista (%)", "porcentaje", false,
                                "Menor que el minorista: el revendedor también tiene que ganar", 20))));
    }

    private static CampoFormulario unidad(String nombre, String etiqueta, String porDefecto) {
        return CampoFormulario.seleccion(nombre, etiqueta, true, null, porDefecto, Unidades.opciones());
    }

    @Override
    public CostoVariable costoVariable(EntradaFabricacion e, ComunesResueltos c) {
        if (e.materiasPrimas().isEmpty() && e.insumos().isEmpty() && e.minutosManoObra().signum() == 0) {
            throw new RegraDeNegocioException(t("Cargá al menos una materia prima, un insumo o minutos de mano de obra"));
        }
        BigDecimal materias = BigDecimal.ZERO;
        for (MateriaPrima m : e.materiasPrimas()) {
            materias = materias.add(m.unidadCompra().costoProporcional(
                    m.precioCompra(), m.cantidadCompra(), m.consumoPorPieza(), m.unidadConsumo(), m.nombre()));
        }
        BigDecimal merma = materias.multiply(factorMerma(e.mermaPct(), "La merma"), MC).subtract(materias);
        BigDecimal insumos = e.insumos().stream()
                .map((Insumo i) -> i.costoUnitario().multiply(i.cantidadPorPieza()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costoMinuto = e.costoPorMinuto() != null
                ? e.costoPorMinuto()
                : c.valorHora().divide(BigDecimal.valueOf(60), MC);
        BigDecimal manoObra = e.minutosManoObra().multiply(costoMinuto, MC);

        BigDecimal total = materias.add(merma).add(insumos).add(manoObra);
        return new CostoVariable(e.nombreProducto(), total, List.of(
                LineaDesglose.moneda("materiasPrimas", "Materias primas", r(materias)),
                LineaDesglose.moneda("merma", "Merma", r(merma)),
                LineaDesglose.moneda("insumos", "Insumos y avíos", r(insumos)),
                LineaDesglose.moneda("manoObra", "Mano de obra", r(manoObra))));
    }

    @Override
    public PropuestaPrecio proponerPrecio(EntradaFabricacion e, CostoVariable costo, ComunesResueltos c) {
        BigDecimal costoTotal = costo.total().add(rateo(c));
        BigDecimal minorista = precioPorMargen(costoTotal, c.porcentajeSobreVenta(), e.margenMinoristaPct());
        BigDecimal mayorista = precioPorMargen(costoTotal, c.porcentajeSobreVenta(), e.margenMayoristaPct());

        PropuestaPrecio p = new PropuestaPrecio(minorista,
                t("Margen minorista de {0} % sobre el precio", Textos.numero(e.margenMinoristaPct())));
        p.extras().add(LineaDesglose.moneda("precioMayorista", "Precio mayorista", r(mayorista)));
        p.extras().add(LineaDesglose.porcentaje("descuentoMayoristaPct", "Descuento mayorista sobre el minorista",
                r(BigDecimal.ONE.subtract(mayorista.divide(minorista, MC)).multiply(CIEN))));
        p.extras().add(LineaDesglose.numero("markupMinorista", "Markup minorista (precio ÷ costo)",
                r(minorista.divide(costoTotal, MC))));
        if (e.margenMayoristaPct().compareTo(e.margenMinoristaPct()) >= 0) {
            p.avisos().add(t("El margen mayorista debería ser menor que el minorista para que el revendedor gane."));
        }
        if (e.minutosManoObra().signum() > 0 && e.costoPorMinuto() == null && c.valorHora().signum() == 0) {
            p.avisos().add(t("Tu mano de obra quedó en cero: cargá tu valor hora en Configuración."));
        }
        return p;
    }
}
