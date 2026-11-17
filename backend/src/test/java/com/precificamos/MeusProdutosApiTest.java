package com.precificamos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MeusProdutosApiTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String CUERPO = """
            {"segmento":"reventa","nombre":"Camisa",
             "comun":{"costosFijosMensuales":600,"ventasMensualesProducto":3},
             "datos":{"nombreProducto":"Camisa","costoCompra":20,"markup":2},
             "precioSugerido":240.5,"costoTotalUnitario":220,"margenRealPct":8.5,"lucroUnitario":20.5}
            """;

    @Autowired MockMvc mvc;

    private long crear(String auth, String cuerpo) throws Exception {
        String r = mvc.perform(post("/api/meus-produtos").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return MAPPER.readTree(r).get("id").asLong();
    }

    @Test
    void sinSesionDa401() throws Exception {
        mvc.perform(get("/api/meus-produtos")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/meus-produtos").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void guardaListaEditaYBorra() throws Exception {
        String auth = Sesiones.bearer(Sesiones.registrar(mvc));
        long id = crear(auth, CUERPO);

        mvc.perform(get("/api/meus-produtos/" + id).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.segmento").value("REVENTA"))
                .andExpect(jsonPath("$.nombre").value("Camisa"))
                .andExpect(jsonPath("$.datos.costoCompra").value(20))
                .andExpect(jsonPath("$.comun.ventasMensualesProducto").value(3))
                // Sin precio final elegido se usa el sugerido.
                .andExpect(jsonPath("$.precioFinal").value(240.5))
                .andExpect(jsonPath("$.creadoEn").exists())
                .andExpect(jsonPath("$.usuarioId").doesNotExist());

        mvc.perform(patch("/api/meus-produtos/" + id).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"precioFinal\":249.9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioFinal").value(249.9))
                .andExpect(jsonPath("$.nombre").value("Camisa"));

        mvc.perform(put("/api/meus-produtos/" + id).header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO.replace("\"Camisa\",\n", "\"Camisa azul\",\n").replace("\"lucroUnitario\":20.5", "\"lucroUnitario\":21,\"precioFinal\":260")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Camisa azul"))
                .andExpect(jsonPath("$.precioFinal").value(260));

        mvc.perform(get("/api/meus-produtos").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(delete("/api/meus-produtos/" + id).header("Authorization", auth)).andExpect(status().isNoContent());
        mvc.perform(get("/api/meus-produtos").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void cadaUsuarioVeSoloLoSuyo() throws Exception {
        String ana = Sesiones.bearer(Sesiones.registrar(mvc));
        String beto = Sesiones.bearer(Sesiones.registrar(mvc));
        long id = crear(ana, CUERPO);

        mvc.perform(get("/api/meus-produtos").header("Authorization", beto))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/meus-produtos/" + id).header("Authorization", beto)).andExpect(status().isNotFound());
        mvc.perform(put("/api/meus-produtos/" + id).header("Authorization", beto)
                .contentType(MediaType.APPLICATION_JSON).content(CUERPO)).andExpect(status().isNotFound());
        mvc.perform(patch("/api/meus-produtos/" + id).header("Authorization", beto)
                .contentType(MediaType.APPLICATION_JSON).content("{\"precioFinal\":1}")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/meus-produtos/" + id).header("Authorization", beto)).andExpect(status().isNotFound());

        // El de Ana sigue intacto.
        mvc.perform(get("/api/meus-produtos/" + id).header("Authorization", ana))
                .andExpect(status().isOk()).andExpect(jsonPath("$.precioFinal").value(240.5));
    }

    @Test
    void validaLosDatos() throws Exception {
        String auth = Sesiones.bearer(Sesiones.registrar(mvc));
        mvc.perform(post("/api/meus-produtos").header("Authorization", auth).header("Accept-Language", "pt-BR")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"segmento\":\"reventa\",\"nombre\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombre").value("Dê um nome ao produto"));
        mvc.perform(post("/api/meus-produtos").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"segmento\":\"no-existe\",\"nombre\":\"X\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.segmento").exists());
        mvc.perform(post("/api/meus-produtos").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"segmento\":\"reventa\",\"nombre\":\"X\",\"precioFinal\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.precioFinal").exists());
    }
}
