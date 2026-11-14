package com.precificamos.auth;

import static com.precificamos.i18n.Textos.t;

/** Ya existe una cuenta con ese email (HTTP 409). */
public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException() { super(t("Ya existe una cuenta con ese email. Probá iniciar sesión.")); }
}
