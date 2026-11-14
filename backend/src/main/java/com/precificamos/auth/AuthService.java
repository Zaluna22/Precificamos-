package com.precificamos.auth;

import com.precificamos.auth.dto.RespuestaAuth;
import com.precificamos.auth.dto.SolicitudLogin;
import com.precificamos.auth.dto.SolicitudRegistro;
import com.precificamos.auth.dto.UsuarioDto;
import com.precificamos.model.Usuario;
import com.precificamos.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Alta de cuentas, inicio de sesión y datos del usuario actual. */
@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public RespuestaAuth registrar(SolicitudRegistro s) {
        String email = normalizar(s.email());
        if (usuarios.existsByEmail(email)) throw new EmailYaRegistradoException();
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setNombre(s.nombre().trim());
        u.setPasswordHash(encoder.encode(s.password()));
        try {
            u = usuarios.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            // Dos registros simultáneos con el mismo email: gana el primero.
            throw new EmailYaRegistradoException();
        }
        return new RespuestaAuth(jwt.generar(u), UsuarioDto.de(u));
    }

    public RespuestaAuth login(SolicitudLogin s) {
        Usuario u = usuarios.findByEmail(normalizar(s.email()))
                .filter(x -> encoder.matches(s.password(), x.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);
        return new RespuestaAuth(jwt.generar(u), UsuarioDto.de(u));
    }

    public UsuarioDto yo(Long id) {
        return usuarios.findById(id).map(UsuarioDto::de).orElseThrow(NoAutenticadoException::new);
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
