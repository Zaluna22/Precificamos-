package com.precificamos.auth.dto;

import com.precificamos.model.Usuario;

import java.time.LocalDateTime;

/** Datos públicos del usuario (sin la contraseña). */
public record UsuarioDto(Long id, String email, String nombre, LocalDateTime fechaCreacion) {
    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getEmail(), u.getNombre(), u.getFechaCreacion());
    }
}
