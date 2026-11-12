package com.precificamos.segmento.estrategia;

import com.precificamos.segmento.*;
import com.precificamos.segmento.dto.CampoFormulario;
import com.precificamos.segmento.dto.EntradaServicios;
import com.precificamos.segmento.dto.LineaDesglose;
import com.precificamos.segmento.dto.PasoFormulario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.precificamos.segmento.CalculadoraBase.*;

/**
 * Servicios profesionales o técnicos.
 *
 * <p>Costo por servicio = horas × valor hora + materiales + km × costo por km + otros gastos de
 * desplazamiento. El precio sale del margen deseado sobre el precio.</p>
 */
@Component
public class ServiciosEstrategia implements EstrategiaPrecificacion<EntradaServicios> {

    @Override public Segmento segmento() { return Segmento.SERVICIOS; }

    @Override public Class<EntradaServicios> tipoEntrada() { return EntradaServicios.class; }

    @Override
    public List<PasoFormulario> pasos() {
        return List.of(
                new PasoFormulario("Tu servicio", "¿Qué hacés y cuánto tiempo te lleva?", List.of(
                        CampoFormulario.de("nombreServicio", "Nombre del servicio", "texto", true,
                                "Ej.: Corte y peinado, Instalación eléctrica, Logo para marca", null),
                        CampoFormulario.de("horasPorServicio", "Horas por servicio", "numero", true,
                                "Incluí preparación y limpieza. 1 h 30 min = 1,5", 1),
                        CampoFormulario.de("valorHora", "Valor de tu hora", "moneda", false,
                                "Vacío = el valor hora de Configuración", null))),
                new PasoFormulario("Materiales", "Lo que gastás en cada servicio.", List.of(
                        CampoFormulario.lista("materiales", "Materiales", false, "Ej.: Tintura, 18", List.of(
                                CampoFormulario.de("nombre", "Material", "texto", true, null, null),
                                CampoFormulario.de("costo", "Costo por servicio", "moneda", true, null, null))))),
                new PasoFormulario("Desplazamiento", "Si vas a domicilio o al cliente.", List.of(
                        CampoFormulario.de("kmDesplazamiento", "Kilómetros (ida y vuelta)", "numero", false, null, 0),
                        CampoFormulario.de("costoPorKm", "Costo por km", "moneda", false,
                                "Combustible + desgaste. Un auto ronda 0,80 a 1,20 por km", 0),
                        CampoFormulario.de("otrosGastosDesplazamiento", "Peajes, estacionamiento, pasajes", "moneda", false, null, 0))),
                new PasoFormulario("Margen", "Lo que querés que quede de ganancia.", List.of(
                        CampoFormulario.de("margenPct", "Margen deseado (%)", "porcentaje", false,
                                "Sobre el precio final, después de impuestos y tasas", 30))));
    }

    @Override
    public CostoVariable costoVariable(EntradaServicios e, ComunesResueltos c) {
        BigDecimal valorHora = e.valorHora() != null ? e.valorHora() : c.valorHora();
        BigDecimal tiempo = e.horasPorServicio().multiply(valorHora, MC);
        BigDecimal materiales = e.materiales().stream().map(EntradaServicios.Material::costo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal desplazamiento = e.kmDesplazamiento().multiply(e.costoPorKm()).add(e.otrosGastosDesplazamiento());
        return new CostoVariable(e.nombreServicio(), tiempo.add(materiales).add(desplazamiento), List.of(
                LineaDesglose.moneda("tiempo", "Tu tiempo", r(tiempo)),
                LineaDesglose.moneda("materiales", "Materiales", r(materiales)),
                LineaDesglose.moneda("desplazamiento", "Desplazamiento", r(desplazamiento))));
    }

    @Override
    public PropuestaPrecio proponerPrecio(EntradaServicios e, CostoVariable costo, ComunesResueltos c) {
        BigDecimal precio = precioPorMargen(costo.total().add(rateo(c)), c.porcentajeSobreVenta(), e.margenPct());
        PropuestaPrecio p = new PropuestaPrecio(precio, "Margen de "
                + e.margenPct().stripTrailingZeros().toPlainString() + " % sobre el precio");
        p.extras().add(LineaDesglose.moneda("precioPorHora", "Precio equivalente por hora",
                r(precio.divide(e.horasPorServicio(), MC))));
        BigDecimal valorHora = e.valorHora() != null ? e.valorHora() : c.valorHora();
        if (valorHora.signum() == 0) {
            p.avisos().add("Tu hora quedó en cero: cargá cuánto vale tu hora para no trabajar gratis.");
        }
        return p;
    }
}
