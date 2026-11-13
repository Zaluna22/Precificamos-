package com.precificamos.i18n;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Currency;
import java.util.Locale;

/**
 * Idiomas de la API. El español es el idioma original de todos los textos; portugués (Brasil)
 * e inglés se traducen con los diccionarios de {@code resources/i18n}.
 *
 * <p>Cada idioma define también cómo se muestran los montos: español y portugués siguen en
 * reales (como hasta ahora) y el inglés usa dólares con formato de EE. UU.</p>
 */
public enum Idioma {
    ES("es", Locale.forLanguageTag("es"), "BRL", Locale.forLanguageTag("pt-BR")),
    PT("pt-BR", Locale.forLanguageTag("pt-BR"), "BRL", Locale.forLanguageTag("pt-BR")),
    EN("en", Locale.ENGLISH, "USD", Locale.US);

    private final String etiqueta;
    private final Locale locale;
    private final Currency moneda;
    private final Locale localeMoneda;

    Idioma(String etiqueta, Locale locale, String moneda, Locale localeMoneda) {
        this.etiqueta = etiqueta;
        this.locale = locale;
        this.moneda = Currency.getInstance(moneda);
        this.localeMoneda = localeMoneda;
    }

    /** Etiqueta BCP 47 ({@code es}, {@code pt-BR}, {@code en}). */
    public String getEtiqueta() { return etiqueta; }
    public Locale getLocale() { return locale; }
    public Currency getMoneda() { return moneda; }
    public Locale getLocaleMoneda() { return localeMoneda; }

    /** Interpreta {@code es}, {@code es-AR}, {@code pt}, {@code pt-BR}, {@code pt_br}, {@code en-US}…; nulo si no es soportado. */
    public static Idioma desde(String etiqueta) {
        if (etiqueta == null) return null;
        String e = etiqueta.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        if (e.equals("pt") || e.startsWith("pt-")) return PT;
        if (e.equals("en") || e.startsWith("en-")) return EN;
        if (e.equals("es") || e.startsWith("es-")) return ES;
        return null;
    }

    public static Idioma desde(Locale locale) {
        if (locale == null) return ES;
        Idioma i = desde(locale.toLanguageTag());
        return i == null ? ES : i;
    }

    /**
     * Idioma de la solicitud en curso (lo fija {@link IdiomaLocaleResolver}). Fuera de una
     * solicitud, por ejemplo en las pruebas unitarias de las estrategias, es español.
     */
    public static Idioma actual() {
        LocaleContext ctx = LocaleContextHolder.getLocaleContext();
        return ctx == null ? ES : desde(ctx.getLocale());
    }
}
