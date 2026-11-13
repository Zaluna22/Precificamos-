package com.precificamos.auth;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Acceso al usuario autenticado de la solicitud actual. */
@Component
public class UsuarioActual {

    /** Id del usuario logueado, o vacío si la solicitud es anónima. */
    public Optional<Long> id() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a instanceof AnonymousAuthenticationToken || !(a.getPrincipal() instanceof Long id)) {
            return Optional.empty();
        }
        return Optional.of(id);
    }

    /** Id del usuario logueado; si no hay sesión lanza {@link NoAutenticadoException} (401). */
    public Long requerido() {
        return id().orElseThrow(NoAutenticadoException::new);
    }

    /** Id del usuario o {@code null} si es anónimo (para los cálculos públicos). */
    public Long oNull() {
        return id().orElse(null);
    }
}
