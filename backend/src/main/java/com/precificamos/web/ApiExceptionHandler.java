package com.precificamos.web;

import com.precificamos.service.RecursoNaoEncontradoException;
import com.precificamos.service.RegraDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RegraDeNegocioException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> regra(RegraDeNegocioException e) { return Map.of("erro", e.getMessage()); }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> naoEncontrado(RecursoNaoEncontradoException e) { return Map.of("erro", e.getMessage()); }
}
