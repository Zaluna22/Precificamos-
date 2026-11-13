package com.precificamos.auth;

/** Ya existe una cuenta con ese email (HTTP 409). */
public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException() { super("Ya existe una cuenta con ese email. Probá iniciar sesión."); }
}
