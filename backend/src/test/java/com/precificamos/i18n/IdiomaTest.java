package com.precificamos.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class IdiomaTest {

    @Test
    void interpretaEtiquetas() {
        assertEquals(Idioma.PT, Idioma.desde("pt"));
        assertEquals(Idioma.PT, Idioma.desde("pt_BR"));
        assertEquals(Idioma.PT, Idioma.desde("PT-br"));
        assertEquals(Idioma.EN, Idioma.desde("en-GB"));
        assertEquals(Idioma.ES, Idioma.desde("es-AR"));
        assertNull(Idioma.desde("fr"));
        assertNull(Idioma.desde((String) null));
    }

    @Test
    void fueraDeUnaSolicitudEsEspanol() {
        assertEquals(Idioma.ES, Idioma.actual());
        assertEquals("Margen de 30 % sobre el precio", Textos.t("Margen de {0} % sobre el precio", 30));
    }

    @Test
    void resolvedorUsaParametroCabeceraYEspanolPorDefecto() {
        MockHttpServletRequest r = new MockHttpServletRequest();
        assertEquals(Idioma.ES, IdiomaLocaleResolver.idioma(r));
        r.addHeader("Accept-Language", "en-US,en;q=0.9");
        assertEquals(Idioma.EN, IdiomaLocaleResolver.idioma(r));
        r.setParameter("lang", "pt-BR");
        assertEquals(Idioma.PT, IdiomaLocaleResolver.idioma(r));

        MockHttpServletRequest mal = new MockHttpServletRequest();
        mal.addHeader("Accept-Language", ";;;q=x");
        assertEquals(Idioma.ES, IdiomaLocaleResolver.idioma(mal));
    }

    @Test
    void monedaYNumerosSegunIdioma() {
        org.springframework.context.i18n.LocaleContextHolder.setLocale(Locale.ENGLISH);
        try {
            assertEquals("$1,234.50", Textos.moneda(new BigDecimal("1234.5")));
            assertEquals("12.5", Textos.numero(new BigDecimal("12.50")));
        } finally {
            org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext();
        }
        org.springframework.context.i18n.LocaleContextHolder.setLocale(Locale.forLanguageTag("pt-BR"));
        try {
            assertEquals("R$ 1.234,50", Textos.moneda(new BigDecimal("1234.5")));
            assertEquals("12,5", Textos.numero(new BigDecimal("12.50")));
        } finally {
            org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext();
        }
        assertEquals("R$ 1.234,50", Textos.moneda(new BigDecimal("1234.5")));
    }
}
