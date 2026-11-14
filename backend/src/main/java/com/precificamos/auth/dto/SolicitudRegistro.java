package com.precificamos.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear una cuenta. */
public record SolicitudRegistro(
        @NotBlank(message = "Ingresá tu email")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 254, message = "El email es demasiado largo")
        String email,
        @NotBlank(message = "Ingresá tu nombre")
        @Size(max = 100, message = "El nombre puede tener hasta 100 caracteres")
        String nombre,
        @NotBlank(message = "Ingresá una contraseña")
        @Size(min = 8, max = 72, message = "La contraseña tiene que tener entre 8 y 72 caracteres")
        String password) {}
