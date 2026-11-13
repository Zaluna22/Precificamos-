package com.precificamos.i18n;

import com.precificamos.segmento.EstrategiaPrecificacion;
import com.precificamos.segmento.Segmento;
import com.precificamos.segmento.dto.CampoFormulario;
import com.precificamos.segmento.dto.PasoFormulario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica que los diccionarios de portugués e inglés tengan todos los textos de la API. */
@SpringBootTest
class DiccionariosTest {
    @Autowired List<EstrategiaPrecificacion<?>> estrategias;

    @Test
    void ambosDiccionariosTienenLasMismasClavesYMarcadores() {
        Map<String, String> pt = Textos.diccionario(Idioma.PT);
        Map<String, String> en = Textos.diccionario(Idioma.EN);
        assertEquals(pt.keySet(), en.keySet());
        Pattern marcador = Pattern.compile("\\{\\d}");
        for (String clave : pt.keySet()) {
            Set<String> esperados = marcadores(marcador, clave);
            assertEquals(esperados, marcadores(marcador, pt.get(clave)), "pt-BR: " + clave);
            assertEquals(esperados, marcadores(marcador, en.get(clave)), "en: " + clave);
            assertFalse(pt.get(clave).isBlank(), clave);
            assertFalse(en.get(clave).isBlank(), clave);
        }
    }

    @Test
    void todosLosTextosDeLosFormulariosEstanTraducidos() {
        List<String> faltan = new ArrayList<>();
        for (Segmento s : Segmento.values()) {
            revisar(s.getNombre(), faltan);
            revisar(s.getDescripcion(), faltan);
            s.getEjemplos().forEach(e -> revisar(e, faltan));
        }
        for (EstrategiaPrecificacion<?> e : estrategias) {
            for (PasoFormulario p : e.pasos()) {
                revisar(p.titulo(), faltan);
                revisar(p.descripcion(), faltan);
                p.campos().forEach(c -> revisar(c, faltan));
            }
        }
        assertTrue(faltan.isEmpty(), "Faltan traducciones: " + faltan);
    }

    /** Todo texto que el código pasa a t("...") tiene que estar en los diccionarios. */
    @Test
    void todosLosTextosDelCodigoEstanTraducidos() throws IOException {
        Pattern llamada = Pattern.compile("\\bt\\(\"((?:[^\"\\\\]|\\\\.)*)\"");
        List<String> faltan = new ArrayList<>();
        int encontrados = 0;
        try (Stream<Path> archivos = Files.walk(Path.of("src/main/java"))) {
            for (Path a : archivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                // Se quitan los comentarios para no tomar los ejemplos de la documentación.
                String codigo = Files.readString(a).replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\n]*", "");
                Matcher m = llamada.matcher(codigo);
                while (m.find()) {
                    encontrados++;
                    revisar(m.group(1).replace("\\\"", "\""), faltan);
                }
            }
        }
        assertTrue(encontrados > 40, "Se esperaban más llamadas a t(): " + encontrados);
        assertTrue(faltan.isEmpty(), "Faltan traducciones: " + faltan);
    }

    private static void revisar(CampoFormulario c, List<String> faltan) {
        revisar(c.etiqueta(), faltan);
        revisar(c.ayuda(), faltan);
        if (c.opciones() != null) c.opciones().forEach(o -> revisar(o.etiqueta(), faltan));
        if (c.subcampos() != null) c.subcampos().forEach(s -> revisar(s, faltan));
    }

    private static void revisar(String texto, List<String> faltan) {
        if (texto == null) return;
        for (Idioma i : List.of(Idioma.PT, Idioma.EN)) {
            if (!Textos.tieneTraduccion(i, texto)) faltan.add(i.getEtiqueta() + ": " + texto);
        }
    }

    private static Set<String> marcadores(Pattern p, String texto) {
        Set<String> r = new TreeSet<>();
        Matcher m = p.matcher(texto);
        while (m.find()) r.add(m.group());
        return r;
    }
}
