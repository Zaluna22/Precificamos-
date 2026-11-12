package com.precificamos.segmento.estrategia;

import com.precificamos.segmento.UnidadMedida;
import com.precificamos.segmento.dto.CampoFormulario.Opcion;

import java.util.List;

/** Opciones de unidad de medida para los formularios, con nombres para mostrar. */
final class Unidades {
    private Unidades() {}

    static List<Opcion> opciones() {
        return List.of(
                op(UnidadMedida.G, "gramos (g)"), op(UnidadMedida.KG, "kilos (kg)"),
                op(UnidadMedida.ML, "mililitros (ml)"), op(UnidadMedida.L, "litros (l)"),
                op(UnidadMedida.CM, "centímetros (cm)"), op(UnidadMedida.M, "metros (m)"),
                op(UnidadMedida.CM2, "cm²"), op(UnidadMedida.M2, "m²"),
                op(UnidadMedida.UNIDAD, "unidades"), op(UnidadMedida.DOCENA, "docenas"));
    }

    private static Opcion op(UnidadMedida u, String etiqueta) {
        return new Opcion(u.name(), etiqueta, u.getDimension().name());
    }
}
