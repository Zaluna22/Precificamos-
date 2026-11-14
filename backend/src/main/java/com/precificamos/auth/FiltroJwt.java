package com.precificamos.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee la cabecera {@code Authorization: Bearer ...} y, si el token es válido, deja al usuario
 * autenticado para esta solicitud. Un token inválido o vencido se ignora: en los endpoints
 * públicos (calcular, simular) se sigue como anónimo y en los protegidos responde 401.
 */
public class FiltroJwt extends OncePerRequestFilter {
    private static final String PREFIJO = "Bearer ";
    private final JwtService jwt;

    public FiltroJwt(JwtService jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            jwt.validar(cabecera.substring(PREFIJO.length()).trim()).ifPresent(id -> {
                var auth = new UsernamePasswordAuthenticationToken(id, null, AuthorityUtils.createAuthorityList("ROLE_USUARIO"));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(req, res);
    }
}
