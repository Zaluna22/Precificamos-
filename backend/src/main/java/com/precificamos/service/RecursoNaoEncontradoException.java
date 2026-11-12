package com.precificamos.service;

/** Se lanza cuando el recurso pedido no existe (HTTP 404). */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String msg) { super(msg); }
}
