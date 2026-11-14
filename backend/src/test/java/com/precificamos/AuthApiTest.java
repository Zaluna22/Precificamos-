package com.precificamos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Registro, login, sesión y aislamiento de datos entre usuarios. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTest {
    @Autowired MockMvc mvc;

    private static String cuerpo(String email, String nombre, String password) {
        return "{\"email\":\"" + email + "\",\"nombre\":\"" + nombre + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    void registroDevuelveTokenYUsuarioSinPassword() throws Exception {
        String email = Sesiones.emailNuevo();
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(email.toUpperCase(), "Ana", "clave-segura-1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.usuario.email").value(email))
                .andExpect(jsonPath("$.usuario.nombre").value("Ana"))
                .andExpect(jsonPath("$.usuario.fechaCreacion").exists())
                .andExpect(jsonPath("$.usuario.password").doesNotExist())
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist());
    }

    @Test
    void loginCorrectoYYoDevuelveElUsuario() throws Exception {
        String email = Sesiones.emailNuevo();
        Sesiones.registrar(mvc, email, "clave-segura-1");
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"clave-segura-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.email").value(email))
                .andReturn().getResponse().getContentAsString();
        String token = respuesta.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        mvc.perform(get("/api/auth/yo").header("Authorization", Sesiones.bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void passwordIncorrectaDa401() throws Exception {
        String email = Sesiones.emailNuevo();
        Sesiones.registrar(mvc, email, "clave-segura-1");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"otra-clave-9\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Email o contraseña incorrectos"));
    }

    @Test
    void emailInexistenteDa401ConElMismoMensaje() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nadie@ejemplo.com\",\"password\":\"clave-segura-1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Email o contraseña incorrectos"));
    }

    @Test
    void emailDuplicadoDa409() throws Exception {
        String email = Sesiones.emailNuevo();
        Sesiones.registrar(mvc, email, "clave-segura-1");
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(email.toUpperCase(), "Otra", "clave-segura-2")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro", containsString("Ya existe una cuenta")));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "email inválido|no-es-email|Ana|clave-segura-1|email",
            "password corta|ok@ejemplo.com|Ana|corta|password",
            "nombre vacío|ok2@ejemplo.com||clave-segura-1|nombre"})
    void registroValidaLosDatos(String caso, String email, String nombre, String password, String campo) throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(email, nombre == null ? "" : nombre, password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isString())
                .andExpect(jsonPath("$.campos." + campo).isString());
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "GET,/api/auth/yo", "GET,/api/custos-fixos", "POST,/api/custos-fixos", "GET,/api/configuracao",
            "PUT,/api/configuracao", "GET,/api/produtos", "POST,/api/produtos"})
    void endpointsProtegidosDan401SinToken(String metodo, String url) throws Exception {
        mvc.perform(request(org.springframework.http.HttpMethod.valueOf(metodo), url)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro", containsString("iniciar sesión")));
    }

    @Test
    void tokenInvalidoEnProtegidoDa401() throws Exception {
        mvc.perform(get("/api/custos-fixos").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadaUsuarioVeSoloSusDatos() throws Exception {
        String ana = Sesiones.bearer(Sesiones.registrar(mvc));
        String beto = Sesiones.bearer(Sesiones.registrar(mvc));

        String costo = mvc.perform(post("/api/custos-fixos").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Alquiler de Ana\",\"valorMensal\":800}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idCosto = costo.replaceAll(".*\"id\":(\\d+).*", "$1");
        mvc.perform(put("/api/configuracao").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"valorHora\":55,\"impostoPct\":6}")).andExpect(status().isOk());
        String producto = mvc.perform(post("/api/produtos").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Moño\",\"vendasMensaisPrevistas\":10}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idProducto = producto.replaceAll(".*\"id\":(\\d+).*", "$1");

        // Ana ve lo suyo.
        mvc.perform(get("/api/custos-fixos").header("Authorization", ana))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Alquiler de Ana"))
                .andExpect(jsonPath("$[0].usuarioId").doesNotExist());

        // Beto no ve, no borra ni usa nada de Ana.
        mvc.perform(get("/api/custos-fixos").header("Authorization", beto)).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(delete("/api/custos-fixos/" + idCosto).header("Authorization", beto)).andExpect(status().isNotFound());
        mvc.perform(get("/api/configuracao").header("Authorization", beto))
                .andExpect(jsonPath("$.valorHora").value(0))
                .andExpect(jsonPath("$.impostoPct").value(0));
        mvc.perform(get("/api/produtos").header("Authorization", beto)).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/produtos/" + idProducto).header("Authorization", beto)).andExpect(status().isNotFound());
        mvc.perform(put("/api/produtos/" + idProducto).header("Authorization", beto).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Robado\"}")).andExpect(status().isNotFound());

        // Los valores por defecto del formulario usan los costos de quien pregunta.
        mvc.perform(get("/api/segmentos/SERVICIOS").header("Authorization", ana))
                .andExpect(jsonPath("$.pasos[-1].campos[?(@.nombre=='costosFijosMensuales')].valorPorDefecto", contains(800.0)));
        mvc.perform(get("/api/segmentos/SERVICIOS").header("Authorization", beto))
                .andExpect(jsonPath("$.pasos[-1].campos[?(@.nombre=='costosFijosMensuales')].valorPorDefecto", contains(0.0)));

        // Ana sigue teniendo su costo y lo puede borrar.
        mvc.perform(delete("/api/custos-fixos/" + idCosto).header("Authorization", ana)).andExpect(status().isNoContent());
    }

    @Test
    void preflightCorsConAuthorizationNoPideLogin() throws Exception {
        mvc.perform(options("/api/custos-fixos")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Headers", containsStringIgnoringCase("authorization")));
    }
}
