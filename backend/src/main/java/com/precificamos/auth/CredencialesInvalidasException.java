package com.precificamos.auth;

import static com.precificamos.i18n.Textos.t;

/** Email o contraseña incorrectos (HTTP 401). No dice cuál de los dos falló, a propósito. */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() { super(t("Email o contraseña incorrectos")); }
}
