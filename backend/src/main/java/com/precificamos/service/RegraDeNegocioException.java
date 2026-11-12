package com.precificamos.service;

/** Se lanza cuando los datos violan una regla de negocio del cálculo (HTTP 422). */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String msg) { super(msg); }
}
