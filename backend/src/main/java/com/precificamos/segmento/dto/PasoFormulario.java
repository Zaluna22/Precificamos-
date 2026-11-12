package com.precificamos.segmento.dto;

import java.util.List;

/** Un paso del formulario guiado: título, explicación y los campos que se piden en él. */
public record PasoFormulario(String titulo, String descripcion, List<CampoFormulario> campos) {}
