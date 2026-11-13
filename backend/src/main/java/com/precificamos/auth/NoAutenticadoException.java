package com.precificamos.auth;

import static com.precificamos.i18n.Textos.t;

/** Se lanza cuando un endpoint necesita sesión y la solicitud no la tiene (HTTP 401). */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException() { super(t("Tenés que iniciar sesión para usar esta función")); }
}
