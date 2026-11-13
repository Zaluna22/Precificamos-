package com.precificamos.auth;

import com.precificamos.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Emite y valida los JWT (HS256) que identifican al usuario en cada solicitud. */
@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String EMISOR = "precificamos-api";
    static final String SECRETO_DESARROLLO = "solo-para-desarrollo-local-cambiar-en-produccion-0123456789";

    private final SecretKey clave;
    private final Duration duracion;

    public JwtService(@Value("${app.jwt.secreto}") String secreto,
                      @Value("${app.jwt.horas-expiracion:168}") long horas) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET tiene que tener al menos 32 caracteres");
        }
        if (SECRETO_DESARROLLO.equals(secreto)) {
            log.warn("Se está usando el secreto JWT de desarrollo. Definí JWT_SECRET en producción.");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.duracion = Duration.ofHours(horas);
    }

    /** Token firmado con el id del usuario como {@code sub}. */
    public String generar(Usuario u) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .issuer(EMISOR)
                .subject(String.valueOf(u.getId()))
                .claim("email", u.getEmail())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    /** Id del usuario si el token es válido y no venció; vacío en cualquier otro caso. */
    public Optional<Long> validar(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).requireIssuer(EMISOR).build()
                    .parseSignedClaims(token).getPayload();
            return Optional.of(Long.valueOf(c.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
