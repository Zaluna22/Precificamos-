package com.precificamos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Configuración de CORS para la API.
 *
 * <p>Los orígenes permitidos se leen de la variable de entorno {@code FRONTEND_URL}
 * (propiedad {@code app.cors.origenes}). Acepta varios valores separados por coma y
 * patrones con comodín, por ejemplo:
 * {@code https://precificamos.vercel.app,https://precificamos-*.vercel.app}.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origenes;

    public CorsConfig(@Value("${app.cors.origenes}") String origenes) {
        this.origenes = Arrays.stream(origenes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                // Se quita la barra final para que coincida con la cabecera Origin del navegador.
                .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origenes)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
