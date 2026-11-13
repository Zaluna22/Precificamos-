package com.precificamos.auth;

/** Se lanza cuando un endpoint necesita sesión y la solicitud no la tiene (HTTP 401). */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException() { super("Tenés que iniciar sesión para usar esta función"); }
}
