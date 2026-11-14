package com.precificamos.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Traducción de los textos de la API. El texto en español es la clave: los diccionarios
 * {@code i18n/pt-BR.json} e {@code i18n/en.json} lo asocian a su traducción. Si falta una
 * traducción se devuelve el texto en español.
 *
 * <p>Los textos con datos variables usan marcadores {@code {0}}, {@code {1}}…:
 * {@code t("Tenés stock para {0} días", dias)}.</p>
 */
public final class Textos {
    private static final Map<Idioma, Map<String, String>> DICCIONARIOS = cargar();

    private Textos() {}

    /** Traduce al idioma de la solicitud actual. */
    public static String t(String es, Object... args) {
        return t(Idioma.actual(), es, args);
    }

    public static String t(Idioma idioma, String es, Object... args) {
        if (es == null) return null;
        String plantilla = idioma == Idioma.ES ? es : DICCIONARIOS.get(idioma).getOrDefault(es, es);
        for (int i = 0; i < args.length; i++) {
            plantilla = plantilla.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return plantilla;
    }

    /** Si el diccionario del idioma tiene el texto (el español siempre lo tiene). */
    public static boolean tieneTraduccion(Idioma idioma, String es) {
        return idioma == Idioma.ES || DICCIONARIOS.get(idioma).containsKey(es);
    }

    /** Diccionario completo de un idioma (para las pruebas). */
    public static Map<String, String> diccionario(Idioma idioma) {
        return idioma == Idioma.ES ? Map.of() : DICCIONARIOS.get(idioma);
    }

    /** Número con hasta 2 decimales en el formato del idioma actual (12,5 o 12.5). */
    public static String numero(BigDecimal valor) {
        NumberFormat f = NumberFormat.getNumberInstance(Idioma.actual().getLocale());
        f.setMaximumFractionDigits(2);
        return f.format(valor);
    }

    /** Monto en la moneda del idioma actual: R$ 12,34 en español y portugués, $12.34 en inglés. */
    public static String moneda(BigDecimal valor) {
        Idioma i = Idioma.actual();
        NumberFormat f = NumberFormat.getCurrencyInstance(i.getLocaleMoneda());
        f.setCurrency(i.getMoneda());
        // Java separa "R$" del número con un espacio duro: se usa uno común para que se lea igual en todos lados.
        return f.format(valor).replace('\u00A0', ' ');
    }

    private static Map<Idioma, Map<String, String>> cargar() {
        ObjectMapper mapper = new ObjectMapper();
        Map<Idioma, Map<String, String>> m = new EnumMap<>(Idioma.class);
        for (Idioma i : Idioma.values()) {
            if (i == Idioma.ES) continue;
            String ruta = "/i18n/" + i.getEtiqueta() + ".json";
            try (InputStream in = Textos.class.getResourceAsStream(ruta)) {
                if (in == null) throw new IllegalStateException("Falta el diccionario " + ruta);
                m.put(i, Collections.unmodifiableMap(mapper.readValue(in, new TypeReference<Map<String, String>>() {})));
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer el diccionario " + ruta, e);
            }
        }
        return m;
    }
}
