package com.precificamos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProdutoApiTest {
    @Autowired MockMvc mvc;

    @Test
    void criaProdutoECalculaPreco() throws Exception {
        mvc.perform(post("/api/custos-fixos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Aluguel\",\"valorMensal\":100}")).andExpect(status().isCreated());

        String body = mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Lacinho\",\"vendasMensaisPrevistas\":50," +
                                "\"materiais\":[{\"nome\":\"Fita\",\"precoPorKg\":50,\"quantidadeGramas\":20}]}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/produtos/" + id + "/preco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoSugerido").isNumber());

        mvc.perform(get("/api/produtos/" + id + "/preco").param("valor", "99"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void produtoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/produtos/9999")).andExpect(status().isNotFound());
    }
}
