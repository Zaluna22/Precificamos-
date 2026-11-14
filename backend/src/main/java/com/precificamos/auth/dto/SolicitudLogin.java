package com.precificamos.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos para iniciar sesión. */
public record SolicitudLogin(
        @NotBlank(message = "Ingresá tu email") String email,
        @NotBlank(message = "Ingresá tu contraseña") String password) {}
