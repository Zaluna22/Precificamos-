package com.precificamos.auth;

import com.precificamos.auth.dto.RespuestaAuth;
import com.precificamos.auth.dto.SolicitudLogin;
import com.precificamos.auth.dto.SolicitudRegistro;
import com.precificamos.auth.dto.UsuarioDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Cuentas de usuario.
 *
 * <ul>
 *   <li>{@code POST /api/auth/registro}: crea la cuenta y devuelve token + usuario.</li>
 *   <li>{@code POST /api/auth/login}: devuelve token + usuario.</li>
 *   <li>{@code GET /api/auth/yo}: usuario del token (requiere sesión).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    private final UsuarioActual actual;

    public AuthController(AuthService service, UsuarioActual actual) {
        this.service = service;
        this.actual = actual;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaAuth registro(@Valid @RequestBody SolicitudRegistro s) { return service.registrar(s); }

    @PostMapping("/login")
    public RespuestaAuth login(@Valid @RequestBody SolicitudLogin s) { return service.login(s); }

    @GetMapping("/yo")
    public UsuarioDto yo() { return service.yo(actual.requerido()); }
}
