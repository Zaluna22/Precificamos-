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

/** Pruebas de punta a punta de /api/segmentos. */
@SpringBootTest
@AutoConfigureMockMvc
class SegmentoApiTest {
    @Autowired MockMvc mvc;

    private static final String COMUN = "\"comun\":{\"costosFijosMensuales\":1000,\"impuestoPct\":6,\"tarjetaPct\":4,"
            + "\"marketplacePct\":0,\"comisionPct\":0,\"ventasMensualesProducto\":100,\"unidadesMensualesNegocio\":500}";

    @Test
    void listaCincoCategoriasConEjemplosYPasoComun() throws Exception {
        mvc.perform(get("/api/segmentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].codigo", contains("PRODUCCION_ALIMENTOS", "FABRICACION", "REVENTA",
                        "VENTA_MASIVA", "SERVICIOS")))
                .andExpect(jsonPath("$[0].ejemplos", hasItem("Panadería")))
                .andExpect(jsonPath("$[1].ejemplos", hasItem("Ropa y confección")))
                .andExpect(jsonPath("$[3].ejemplos", hasItem("Supermercado")))
                .andExpect(jsonPath("$[0].pasos[-1].campos[0].grupo").value("comun"))
                .andExpect(jsonPath("$[0].pasos[1].campos[0].tipo").value("lista"));
    }

    @Test
    void obtieneUnaCategoriaConCodigoEnMinusculas() throws Exception {
        mvc.perform(get("/api/segmentos/venta-masiva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Venta masiva / autoservicio"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "PRODUCCION_ALIMENTOS|{\"nombreReceta\":\"Torta\",\"rendimientoPorciones\":10,\"ingredientes\":[{\"nombre\":\"Harina\",\"precioCompra\":6.5,\"cantidadCompra\":1,\"unidadCompra\":\"kg\",\"cantidadUsada\":500,\"unidadUsada\":\"g\"}]}",
            "FABRICACION|{\"nombreProducto\":\"Remera\",\"materiasPrimas\":[{\"nombre\":\"Tela\",\"precioCompra\":30,\"cantidadCompra\":1,\"unidadCompra\":\"M\",\"consumoPorPieza\":80,\"unidadConsumo\":\"CM\"}],\"minutosManoObra\":45,\"costoPorMinuto\":0.5}",
            "REVENTA|{\"nombreProducto\":\"Perfume\",\"costoCompra\":50,\"markup\":2}",
            "VENTA_MASIVA|{\"nombreProducto\":\"Arroz\",\"categoria\":\"ALMACEN\",\"costoCompra\":4.2,\"promoLleva\":3,\"promoPaga\":2}",
            "SERVICIOS|{\"nombreServicio\":\"Corte\",\"horasPorServicio\":1,\"valorHora\":40}"})
    void calculaCadaCategoria(String segmento, String datos) throws Exception {
        mvc.perform(post("/api/segmentos/" + segmento + "/calcular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + COMUN + ",\"datos\":" + datos + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.segmento").value(segmento))
                .andExpect(jsonPath("$.precioSugerido").isNumber())
                .andExpect(jsonPath("$.costoTotalUnitario").isNumber())
                .andExpect(jsonPath("$.margenRealPct").isNumber())
                .andExpect(jsonPath("$.desglose").isNotEmpty());
    }

    @Test
    void serviciosDevuelveValoresExactos() throws Exception {
        // 88 / (1 - 0,10 - 0,30) + prorrateo 2 / 0,9 = 148,89 (los fijos no llevan margen)
        String datos = "{\"nombreServicio\":\"Coloración\",\"horasPorServicio\":1.5,\"valorHora\":30,"
                + "\"materiales\":[{\"nombre\":\"Tintura\",\"costo\":18}],\"kmDesplazamiento\":20,\"costoPorKm\":1,"
                + "\"otrosGastosDesplazamiento\":5,\"margenPct\":30}";
        mvc.perform(post("/api/segmentos/servicios/calcular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + COMUN + ",\"datos\":" + datos + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costoVariableUnitario").value(88.00))
                .andExpect(jsonPath("$.rateoCostosFijos").value(2.00))
                .andExpect(jsonPath("$.precioSugerido").value(148.89))
                .andExpect(jsonPath("$.margenRealPct").value(29.55))
                .andExpect(jsonPath("$.puntoEquilibrioUnidades").value(5))
                .andExpect(jsonPath("$.lucroMensualEstimado").value(4400.10))
                .andExpect(jsonPath("$.avisos", hasSize(0)));
    }

    private static final String REVENTA_SIMPLE = "\"datos\":{\"nombreProducto\":\"Simple\",\"costoCompra\":20,\"markup\":2}";

    private static String comunSimple(String extra) {
        return "\"comun\":{\"costosFijosMensuales\":600,\"impuestoPct\":0,\"tarjetaPct\":0,\"marketplacePct\":0,"
                + "\"comisionPct\":0,\"ventasMensualesProducto\":3" + extra + "}";
    }

    @Test
    void reventaSinTotalDelNegocioCargaTodosLosFijosYAvisa() throws Exception {
        mvc.perform(post("/api/segmentos/REVENTA/calcular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + comunSimple("") + "," + REVENTA_SIMPLE + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rateoCostosFijos").value(200.00))
                .andExpect(jsonPath("$.precioSugerido").value(240.00))      // 20 × 2 + 200
                .andExpect(jsonPath("$.puntoEquilibrioUnidades").value(3))  // 600 / 220
                .andExpect(jsonPath("$.lucroMensualEstimado").value(60.00))
                .andExpect(jsonPath("$.avisos", hasItem(containsString("costos fijos están todos"))));
    }

    @Test
    void reventaConTotalDelNegocioYTarjeta() throws Exception {
        mvc.perform(post("/api/segmentos/REVENTA/calcular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + comunSimple(",\"unidadesMensualesNegocio\":150") + "," + REVENTA_SIMPLE + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioSugerido").value(44.00))       // 20 × 2 + 4
                .andExpect(jsonPath("$.puntoEquilibrioUnidades").value(1))  // (4 × 3) / 24
                .andExpect(jsonPath("$.lucroMensualEstimado").value(60.00))
                .andExpect(jsonPath("$.avisos", hasSize(0)));
        String conTarjeta = comunSimple(",\"unidadesMensualesNegocio\":150").replace("\"tarjetaPct\":0", "\"tarjetaPct\":3.5");
        mvc.perform(post("/api/segmentos/REVENTA/calcular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + conTarjeta + "," + REVENTA_SIMPLE + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioSugerido").value(45.60));      // 44 / 0,965
    }

    @Test
    void avisoDeFijosTraducido() throws Exception {
        mvc.perform(post("/api/segmentos/REVENTA/calcular").header("Accept-Language", "pt-BR")
                        .contentType(MediaType.APPLICATION_JSON).content("{" + comunSimple("") + "," + REVENTA_SIMPLE + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avisos", hasItem("Os custos fixos estão todos neste produto. Se você vende outros produtos, preencha o total de unidades do negócio.")));
    }

    @Test
    void simuladorCoincideConElCalculo() throws Exception {
        mvc.perform(post("/api/segmentos/REVENTA/simular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + comunSimple(",\"unidadesMensualesNegocio\":150") + "," + REVENTA_SIMPLE + ",\"precio\":44}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntoEquilibrioUnidades").value(1))
                .andExpect(jsonPath("$.lucroMensual").value(60.00))
                .andExpect(jsonPath("$.cubreCostos").value(true));
    }

    @Test
    void campoDeUnidadesDelNegocioDestacado() throws Exception {
        mvc.perform(get("/api/segmentos/reventa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pasos[-1].campos[1].nombre").value("unidadesMensualesNegocio"))
                .andExpect(jsonPath("$.pasos[-1].campos[1].destacado").value(true))
                .andExpect(jsonPath("$.pasos[-1].campos[0].destacado").doesNotExist());
    }

    @Test
    void simulaUnPrecioConPerdida() throws Exception {
        String datos = "{\"nombreServicio\":\"Corte\",\"horasPorServicio\":1,\"valorHora\":40}";
        mvc.perform(post("/api/segmentos/SERVICIOS/simular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + COMUN + ",\"datos\":" + datos + ",\"precio\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cubreCostos").value(false))
                .andExpect(jsonPath("$.lucroMensual").value(lessThan(0.0)))
                .andExpect(jsonPath("$.ventasMensuales").value(100));
    }

    @Test
    void simularSinPrecioDa400() throws Exception {
        mvc.perform(post("/api/segmentos/SERVICIOS/simular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + COMUN + ",\"datos\":{\"nombreServicio\":\"x\",\"horasPorServicio\":1}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void segmentoInexistenteDa404() throws Exception {
        mvc.perform(post("/api/segmentos/ASTROLOGIA/calcular").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"datos\":{}}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void camposObligatoriosDevuelvenDetalle() throws Exception {
        mvc.perform(post("/api/segmentos/REVENTA/calcular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + COMUN + ",\"datos\":{\"nombreProducto\":\"\",\"costoCompra\":-1}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos['datos.costoCompra']").exists())
                .andExpect(jsonPath("$.campos['datos.nombreProducto']").exists());
    }

    @Test
    void unidadDesconocidaDa400() throws Exception {
        String datos = "{\"nombreReceta\":\"Torta\",\"rendimientoPorciones\":1,\"ingredientes\":[{\"nombre\":\"Harina\","
                + "\"precioCompra\":6.5,\"cantidadCompra\":1,\"unidadCompra\":\"arroba\",\"cantidadUsada\":1}]}";
        mvc.perform(post("/api/segmentos/PRODUCCION_ALIMENTOS/calcular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + COMUN + ",\"datos\":" + datos + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("arroba")));
    }

    @Test
    void margenImposibleDa422() throws Exception {
        mvc.perform(post("/api/segmentos/SERVICIOS/calcular").contentType(MediaType.APPLICATION_JSON)
                        .content("{" + COMUN + ",\"datos\":{\"nombreServicio\":\"x\",\"horasPorServicio\":1,\"margenPct\":95}}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void usaConfiguracionGuardadaCuandoFaltanComunes() throws Exception {
        String auth = Sesiones.bearer(Sesiones.registrar(mvc));
        mvc.perform(put("/api/configuracao").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"valorHora\":60,\"totalUnidadesMes\":50}")).andExpect(status().isOk());
        mvc.perform(post("/api/segmentos/SERVICIOS/calcular").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comun\":{\"costosFijosMensuales\":0},\"datos\":{\"nombreServicio\":\"Clase\",\"horasPorServicio\":1,\"margenPct\":40}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costoVariableUnitario").value(60.00))
                .andExpect(jsonPath("$.precioSugerido").value(100.00));
    }

    @Test
    void anonimoNoUsaLaConfiguracionDeNadie() throws Exception {
        String auth = Sesiones.bearer(Sesiones.registrar(mvc));
        mvc.perform(put("/api/configuracao").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"valorHora\":60,\"totalUnidadesMes\":50}")).andExpect(status().isOk());
        // Sin token, el valor hora guardado por el otro usuario no se aplica (queda en 0).
        mvc.perform(post("/api/segmentos/SERVICIOS/calcular").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comun\":{\"costosFijosMensuales\":0,\"ventasMensualesProducto\":10},"
                                + "\"datos\":{\"nombreServicio\":\"Clase\",\"horasPorServicio\":1,\"margenPct\":40,"
                                + "\"materiales\":[{\"nombre\":\"Hoja\",\"costo\":6}]}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costoVariableUnitario").value(6.00));
    }

    @Test
    void tokenInvalidoEnCalculoSigueComoAnonimo() throws Exception {
        String datos = "{\"nombreServicio\":\"Corte\",\"horasPorServicio\":1,\"valorHora\":40}";
        mvc.perform(post("/api/segmentos/SERVICIOS/calcular").header("Authorization", "Bearer no-es-un-token")
                        .contentType(MediaType.APPLICATION_JSON).content("{" + COMUN + ",\"datos\":" + datos + "}"))
                .andExpect(status().isOk());
    }
}
