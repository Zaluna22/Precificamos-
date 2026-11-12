package com.precificamos.service;

import java.util.Map;

/** Se lanza cuando los datos enviados no pasan la validación (HTTP 400), con el error de cada campo. */
public class DatosInvalidosException extends RuntimeException {
    private final Map<String, String> campos;

    public DatosInvalidosException(String msg, Map<String, String> campos) {
        super(msg);
        this.campos = campos;
    }

    public Map<String, String> getCampos() { return campos; }
}
