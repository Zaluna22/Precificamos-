package com.precificamos.segmento;

import com.precificamos.segmento.dto.CampoFormulario;
import com.precificamos.segmento.dto.DefinicionSegmento;
import com.precificamos.segmento.dto.LineaDesglose;
import com.precificamos.segmento.dto.PasoFormulario;

import java.util.List;

import static com.precificamos.i18n.Textos.t;

/**
 * Traduce al idioma de la solicitud los textos fijos que las estrategias declaran en español
 * (formularios y conceptos del desglose). Los textos con datos variables ya se traducen donde se
 * arman, con {@code Textos.t(...)}.
 */
final class Traduccion {
    private Traduccion() {}

    static DefinicionSegmento definicion(DefinicionSegmento d) {
        return new DefinicionSegmento(d.codigo(), t(d.nombre()), t(d.descripcion()), d.icono(),
                d.ejemplos().stream().map(e -> t(e)).toList(),
                d.pasos().stream().map(Traduccion::paso).toList());
    }

    private static PasoFormulario paso(PasoFormulario p) {
        return new PasoFormulario(t(p.titulo()), t(p.descripcion()), campos(p.campos()));
    }

    private static List<CampoFormulario> campos(List<CampoFormulario> campos) {
        return campos == null ? null : campos.stream().map(Traduccion::campo).toList();
    }

    private static CampoFormulario campo(CampoFormulario c) {
        List<CampoFormulario.Opcion> opciones = c.opciones() == null ? null : c.opciones().stream()
                .map(o -> new CampoFormulario.Opcion(o.valor(), t(o.etiqueta()), o.extra())).toList();
        return new CampoFormulario(c.nombre(), t(c.etiqueta()), c.tipo(), c.grupo(), c.obligatorio(), t(c.ayuda()),
                c.valorPorDefecto(), opciones, campos(c.subcampos()));
    }

    static List<LineaDesglose> lineas(List<LineaDesglose> lineas) {
        return lineas.stream().map(l -> new LineaDesglose(l.clave(), t(l.concepto()), l.valor(), l.tipo())).toList();
    }
}
