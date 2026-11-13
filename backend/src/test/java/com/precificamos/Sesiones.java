package com.precificamos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Ayudas para las pruebas que necesitan un usuario logueado. */
final class Sesiones {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Sesiones() {}

    /** Email único para que las pruebas no choquen entre sí (comparten la base H2). */
    static String emailNuevo() {
        return "prueba-" + UUID.randomUUID() + "@ejemplo.com";
    }

    /** Registra un usuario nuevo y devuelve su token. */
    static String registrar(MockMvc mvc) throws Exception {
        return registrar(mvc, emailNuevo(), "clave-segura-1");
    }

    static String registrar(MockMvc mvc, String email, String password) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"nombre\":\"Ana\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return MAPPER.readTree(cuerpo).get("token").asText();
    }

    static String bearer(String token) {
        return "Bearer " + token;
    }
}
