package com.precificamos.config;

import com.precificamos.i18n.IdiomaLocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

/** Registra el resolvedor de idioma (?lang= o Accept-Language; español por defecto). */
@Configuration
public class IdiomaConfig {
    @Bean(name = "localeResolver")
    public LocaleResolver localeResolver() {
        return new IdiomaLocaleResolver();
    }
}
