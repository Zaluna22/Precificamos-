package com.precificamos.segmento.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Describe un campo del formulario de un segmento para que el frontend lo dibuje sin
 * conocer de antemano cada categoría.
 *
 * @param nombre          nombre de la propiedad en el JSON
 * @param etiqueta        texto de la pregunta
 * @param tipo            {@code texto}, {@code moneda}, {@code porcentaje}, {@code numero}, {@code entero},
 *                        {@code booleano}, {@code seleccion} o {@code lista}
 * @param grupo           {@code datos} (propio del segmento) o {@code comun}
 * @param obligatorio     si el usuario tiene que completarlo
 * @param ayuda           explicación breve con ejemplo
 * @param valorPorDefecto valor sugerido
 * @param opciones        valores posibles para {@code seleccion}
 * @param subcampos       columnas de cada fila para {@code lista}
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CampoFormulario(
        String nombre, String etiqueta, String tipo, String grupo, boolean obligatorio, String ayuda,
        Object valorPorDefecto, List<Opcion> opciones, List<CampoFormulario> subcampos) {

    /** Opción de un campo de selección. {@code extra} puede llevar, por ejemplo, el margen por defecto. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Opcion(String valor, String etiqueta, Object extra) {}

    public static CampoFormulario de(String nombre, String etiqueta, String tipo, boolean obligatorio,
                                     String ayuda, Object valorPorDefecto) {
        return new CampoFormulario(nombre, etiqueta, tipo, "datos", obligatorio, ayuda, valorPorDefecto, null, null);
    }

    public static CampoFormulario seleccion(String nombre, String etiqueta, boolean obligatorio, String ayuda,
                                            Object valorPorDefecto, List<Opcion> opciones) {
        return new CampoFormulario(nombre, etiqueta, "seleccion", "datos", obligatorio, ayuda, valorPorDefecto, opciones, null);
    }

    public static CampoFormulario lista(String nombre, String etiqueta, boolean obligatorio, String ayuda,
                                        List<CampoFormulario> subcampos) {
        return new CampoFormulario(nombre, etiqueta, "lista", "datos", obligatorio, ayuda, null, null, subcampos);
    }

    /** Copia del campo marcado como parte de los parámetros comunes. */
    public CampoFormulario comun() {
        return new CampoFormulario(nombre, etiqueta, tipo, "comun", obligatorio, ayuda, valorPorDefecto, opciones, subcampos);
    }
}
