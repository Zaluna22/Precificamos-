package com.precificamos.web;

import com.precificamos.service.DatosInvalidosException;
import com.precificamos.service.RecursoNaoEncontradoException;
import com.precificamos.service.RegraDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Traduce las excepciones de la aplicación a respuestas JSON con el campo {@code erro}. */
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RegraDeNegocioException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> regra(RegraDeNegocioException e) { return Map.of("erro", e.getMessage()); }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> naoEncontrado(RecursoNaoEncontradoException e) { return Map.of("erro", e.getMessage()); }

    /** Datos de entrada con formato o valores inválidos: devuelve el mensaje general y el detalle por campo. */
    @ExceptionHandler(DatosInvalidosException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> datosInvalidos(DatosInvalidosException e) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("erro", e.getMessage());
        cuerpo.put("campos", e.getCampos());
        return cuerpo;
    }

    /** JSON mal formado o con tipos incorrectos. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> jsonInvalido(HttpMessageNotReadableException e) {
        return Map.of("erro", "El cuerpo de la solicitud no es un JSON válido");
    }
}
