package com.precificamos.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

/**
 * Elige el idioma de cada solicitud: primero el parámetro {@code ?lang=} y después la cabecera
 * {@code Accept-Language} (en orden de preferencia). Si no hay ninguno soportado, español.
 *
 * <p>No usa el idioma por defecto del servidor: sin cabecera, siempre es español.</p>
 */
public class IdiomaLocaleResolver implements LocaleResolver {
    public static final String PARAMETRO = "lang";

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return idioma(request).getLocale();
    }

    /** Idioma pedido en la solicitud (también lo usa la respuesta 401 de seguridad). */
    public static Idioma idioma(HttpServletRequest request) {
        Idioma porParametro = Idioma.desde(request.getParameter(PARAMETRO));
        if (porParametro != null) return porParametro;
        String cabecera = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (cabecera != null && !cabecera.isBlank()) {
            try {
                for (Locale.LanguageRange r : Locale.LanguageRange.parse(cabecera)) {
                    if (r.getWeight() <= 0) continue;
                    Idioma i = Idioma.desde(r.getRange());
                    if (i != null) return i;
                }
            } catch (IllegalArgumentException e) {
                // Cabecera mal formada: se ignora.
            }
        }
        return Idioma.ES;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("El idioma se elige con ?lang= o con la cabecera Accept-Language");
    }
}
