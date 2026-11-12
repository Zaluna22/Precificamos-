package com.precificamos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Verifica que CORS solo acepte los orígenes configurados en FRONTEND_URL. */
@SpringBootTest
@AutoConfigureMockMvc
class CorsTest {
    @Autowired MockMvc mvc;

    @Test
    void permiteOrigenConfigurado() throws Exception {
        mvc.perform(options("/api/custos-fixos")
                        .header("Origin", "https://precificamos-abc.vercel.app")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://precificamos-abc.vercel.app"));
    }

    @Test
    void rechazaOrigenDesconocido() throws Exception {
        mvc.perform(options("/api/custos-fixos")
                        .header("Origin", "https://sitio-malicioso.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
