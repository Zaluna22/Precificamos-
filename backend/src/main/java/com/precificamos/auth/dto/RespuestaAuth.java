package com.precificamos.auth.dto;

/** Respuesta de registro y login: el token para la cabecera Authorization y el usuario. */
public record RespuestaAuth(String token, UsuarioDto usuario) {}
