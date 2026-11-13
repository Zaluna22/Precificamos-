package com.precificamos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Punto de entrada de la API de Precificamos.
 * Se excluye el usuario en memoria que Spring Security crea por defecto: la API usa JWT propios.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class PrecificamosApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrecificamosApplication.class, args);
    }
}
