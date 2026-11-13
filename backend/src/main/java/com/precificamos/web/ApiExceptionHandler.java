package com.precificamos.web;

import com.precificamos.auth.CredencialesInvalidasException;
import com.precificamos.auth.EmailYaRegistradoException;
import com.precificamos.auth.NoAutenticadoException;
import com.precificamos.service.DatosInvalidosException;
import com.precificamos.service.RecursoNaoEncontradoException;
import com.precificamos.service.RegraDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

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
        return conCampos(e.getMessage(), e.getCampos());
    }

    /** Errores de {@code @Valid} en el cuerpo (registro y login): mismo formato que {@link DatosInvalidosException}. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new TreeMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        String general = campos.size() == 1 ? campos.values().iterator().next() : "Revisá los datos ingresados";
        return conCampos(general, campos);
    }

    @ExceptionHandler({NoAutenticadoException.class, CredencialesInvalidasException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> noAutorizado(RuntimeException e) { return Map.of("erro", e.getMessage()); }

    @ExceptionHandler(EmailYaRegistradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> emailDuplicado(EmailYaRegistradoException e) { return Map.of("erro", e.getMessage()); }

    /** JSON mal formado o con tipos incorrectos. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> jsonInvalido(HttpMessageNotReadableException e) {
        return Map.of("erro", "El cuerpo de la solicitud no es un JSON válido");
    }

    private static Map<String, Object> conCampos(String erro, Map<String, String> campos) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("erro", erro);
        cuerpo.put("campos", campos);
        return cuerpo;
    }
}
