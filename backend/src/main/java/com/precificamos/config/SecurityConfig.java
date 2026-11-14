package com.precificamos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.precificamos.auth.FiltroJwt;
import com.precificamos.auth.JwtService;
import com.precificamos.i18n.IdiomaLocaleResolver;
import com.precificamos.i18n.Textos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

/**
 * Seguridad de la API: sesión sin estado con JWT.
 *
 * <ul>
 *   <li>Públicos: registro, login y todo {@code /api/segmentos/**} (calcular y simular). Si llega un
 *       token válido, el cálculo usa los costos fijos y la configuración de ese usuario.</li>
 *   <li>Con sesión: {@code /api/auth/yo}, costos fijos, configuración y productos (401 sin token).</li>
 * </ul>
 *
 * <p>CORS se sigue configurando en {@link CorsConfig}; Spring Security lo aplica antes de
 * pedir autenticación, así el preflight (OPTIONS) nunca recibe 401.</p>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, JwtService jwt, AuthenticationEntryPoint entrada) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login").permitAll()
                        .requestMatchers("/api/segmentos", "/api/segmentos/**").permitAll()
                        .requestMatchers("/api/auth/yo", "/api/custos-fixos/**", "/api/configuracao/**",
                                "/api/produtos/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(entrada))
                .addFilterBefore(new FiltroJwt(jwt), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Respuesta 401 en JSON con el mismo formato que el resto de los errores ({@code erro}).
     * Corre antes de Spring MVC, así que el idioma se resuelve acá mismo.
     */
    @Bean
    public AuthenticationEntryPoint entradaNoAutenticada(ObjectMapper mapper) {
        return (req, res, ex) -> {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            String mensaje = Textos.t(IdiomaLocaleResolver.idioma(req), "Tenés que iniciar sesión para usar esta función");
            mapper.writeValue(res.getOutputStream(), Map.of("erro", mensaje));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
