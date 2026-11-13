package com.precificamos.auth;

/** Email o contraseña incorrectos (HTTP 401). No dice cuál de los dos falló, a propósito. */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() { super("Email o contraseña incorrectos"); }
}
