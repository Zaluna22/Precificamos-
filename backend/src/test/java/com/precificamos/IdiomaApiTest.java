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

/** Selección de idioma de la API: ?lang=, Accept-Language y español por defecto. */
@SpringBootTest
@AutoConfigureMockMvc
class IdiomaApiTest {
    @Autowired MockMvc mvc;

    private static final String COMUN = "\"comun\":{\"costosFijosMensuales\":1000,\"impuestoPct\":6,\"tarjetaPct\":4,"
            + "\"ventasMensualesProducto\":100,\"unidadesMensualesNegocio\":500}";

    @ParameterizedTest(name = "Accept-Language \"{0}\" → {1}")
    @CsvSource(delimiter = '|', value = {
            "|Producción de alimentos",
            "es|Producción de alimentos",
            "es-AR|Producción de alimentos",
            "pt-BR|Produção de alimentos",
            "pt|Produção de alimentos",
            "pt-BR,pt;q=0.9,en;q=0.8|Produção de alimentos",
            "en|Food production",
            "en-US,en;q=0.9|Food production",
            "fr-FR,en;q=0.5|Food production",
            "fr|Producción de alimentos",
            "de-DE,de;q=0.9|Producción de alimentos"})
    void eligeElIdiomaPorAcceptLanguage(String cabecera, String nombre) throws Exception {
        var pedido = get("/api/segmentos");
        if (cabecera != null) pedido.header("Accept-Language", cabecera);
        mvc.perform(pedido)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value(nombre));
    }

    @Test
    void elParametroLangGanaSobreLaCabecera() throws Exception {
        mvc.perform(get("/api/segmentos/PRODUCCION_ALIMENTOS").param("lang", "en").header("Accept-Language", "pt-BR"))
                .andExpect(jsonPath("$.nombre").value("Food production"))
                .andExpect(jsonPath("$.ejemplos", hasItem("Bakery")));
        mvc.perform(get("/api/segmentos/PRODUCCION_ALIMENTOS").param("lang", "pt-BR"))
                .andExpect(jsonPath("$.nombre").value("Produção de alimentos"));
        // Un lang no soportado se ignora y se usa la cabecera.
        mvc.perform(get("/api/segmentos/PRODUCCION_ALIMENTOS").param("lang", "xx").header("Accept-Language", "en"))
                .andExpect(jsonPath("$.nombre").value("Food production"));
    }

    @Test
    void traduceFormularioCompleto() throws Exception {
        mvc.perform(get("/api/segmentos/PRODUCCION_ALIMENTOS").header("Accept-Language", "pt-BR"))
                .andExpect(jsonPath("$.descripcion", startsWith("Gastronomia")))
                .andExpect(jsonPath("$.ejemplos", hasItem("Padaria")))
                .andExpect(jsonPath("$.pasos[0].titulo").value("Sua receita"))
                .andExpect(jsonPath("$.pasos[1].campos[0].subcampos[1].etiqueta").value("Preço do pacote"))
                .andExpect(jsonPath("$.pasos[1].campos[0].subcampos[3].opciones[1].etiqueta").value("quilos (kg)"))
                .andExpect(jsonPath("$.pasos[-1].titulo").value("Vendas, custos fixos e impostos"));
        mvc.perform(get("/api/segmentos/VENTA_MASIVA").header("Accept-Language", "en"))
                .andExpect(jsonPath("$.pasos[0].campos[1].opciones[0].etiqueta").value("Pantry (dry goods)"))
                .andExpect(jsonPath("$.pasos[2].campos[0].etiqueta").value("Round to .99"))
                // Los códigos no se traducen.
                .andExpect(jsonPath("$.codigo").value("VENTA_MASIVA"))
                .andExpect(jsonPath("$.pasos[0].campos[1].opciones[0].valor").value("ALMACEN"));
    }

    @Test
    void traduceElResultadoDelCalculo() throws Exception {
        String datos = "{\"nombreServicio\":\"Corte\",\"horasPorServicio\":1,\"valorHora\":40,\"margenPct\":30}";
        mvc.perform(post("/api/segmentos/SERVICIOS/calcular").header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON).content("{" + COMUN + ",\"datos\":" + datos + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metodo").value("30% margin on the price"))
                .andExpect(jsonPath("$.desglose[0].concepto").value("Your time"))
                .andExpect(jsonPath("$.extras[0].concepto").value("Equivalent price per hour"))
                // El nombre que cargó el usuario no se traduce.
                .andExpect(jsonPath("$.nombre").value("Corte"));
        mvc.perform(post("/api/segmentos/SERVICIOS/calcular").header("Accept-Language", "pt-BR")
                        .contentType(MediaType.APPLICATION_JSON).content("{" + COMUN + ",\"datos\":" + datos + "}"))
                .andExpect(jsonPath("$.metodo").value("Margem de 30% sobre o preço"))
                .andExpect(jsonPath("$.desglose[0].concepto").value("Seu tempo"));
        mvc.perform(post("/api/segmentos/SERVICIOS/calcular")
                        .contentType(MediaType.APPLICATION_JSON).content("{" + COMUN + ",\"datos\":" + datos + "}"))
                .andExpect(jsonPath("$.metodo").value("Margen de 30 % sobre el precio"))
                .andExpect(jsonPath("$.desglose[0].concepto").value("Tu tiempo"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "es|Considerá una promoción o liquidación hasta R$ 75,00",
            "pt-BR|Considere uma promoção ou liquidação até R$ 75,00",
            "en|Consider a promotion or clearance down to $75.00"})
    void losMontosDeLosAvisosSiguenLaMonedaDelIdioma(String idioma, String texto) throws Exception {
        // Costo 50 + margen mínimo 33,33 % sin tasas → liquidación 75; stock para 300 días.
        String datos = "{\"nombreProducto\":\"Perfume\",\"costoCompra\":50,\"markup\":2,"
                + "\"margenMinimoLiquidacionPct\":33.333333,\"stockActual\":1000}";
        String comun = "\"comun\":{\"costosFijosMensuales\":0,\"ventasMensualesProducto\":100}";
        mvc.perform(post("/api/segmentos/REVENTA/calcular").header("Accept-Language", idioma)
                        .contentType(MediaType.APPLICATION_JSON).content("{" + comun + ",\"datos\":" + datos + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avisos", hasItem(containsString(texto))));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "es|Revisá los datos ingresados|no debe estar vacío",
            "pt-BR|Revise os dados informados|não deve estar em branco",
            "en|Please check the data you entered|must not be blank"})
    void traduceLosErroresDeValidacion(String idioma, String general, String campo) throws Exception {
        mvc.perform(post("/api/segmentos/REVENTA/calcular").header("Accept-Language", idioma)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + COMUN + ",\"datos\":{\"nombreProducto\":\"\",\"costoCompra\":10}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(general))
                .andExpect(jsonPath("$.campos['datos.nombreProducto']").value(campo));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "es|Margen + impuestos/tasas llegan al 100 % o más del precio",
            "pt-BR|Margem + impostos/taxas chegam a 100% ou mais do preço",
            "en|Margin + taxes/fees reach 100% or more of the price"})
    void traduceLasReglasDeNegocio(String idioma, String mensaje) throws Exception {
        mvc.perform(post("/api/segmentos/SERVICIOS/calcular").param("lang", idioma).contentType(MediaType.APPLICATION_JSON)
                        .content("{" + COMUN + ",\"datos\":{\"nombreServicio\":\"x\",\"horasPorServicio\":1,\"margenPct\":95}}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value(mensaje));
    }

    @Test
    void traduceMensajesConDatosVariables() throws Exception {
        String datos = "{\"nombreReceta\":\"Torta\",\"rendimientoPorciones\":1,\"ingredientes\":[{\"nombre\":\"Harina\","
                + "\"precioCompra\":6.5,\"cantidadCompra\":1,\"unidadCompra\":\"arroba\",\"cantidadUsada\":1}]}";
        mvc.perform(post("/api/segmentos/PRODUCCION_ALIMENTOS/calcular").header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON).content("{" + COMUN + ",\"datos\":" + datos + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("Unknown unit of measure: arroba")));
        mvc.perform(get("/api/segmentos/ASTROLOGIA").header("Accept-Language", "pt-BR"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro", startsWith("O segmento 'ASTROLOGIA' não existe")));
    }

    @Test
    void traduceLosErroresDeCuentaYSesion() throws Exception {
        mvc.perform(get("/api/custos-fixos").header("Accept-Language", "en"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("You need to sign in to use this feature"));
        mvc.perform(get("/api/custos-fixos").param("lang", "pt-BR"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Você precisa entrar na sua conta para usar esta função"));
        mvc.perform(post("/api/auth/login").header("Accept-Language", "pt-BR").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nadie@ejemplo.com\",\"password\":\"clave-segura-1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("E-mail ou senha incorretos"));
        mvc.perform(post("/api/auth/registro").header("Accept-Language", "en").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ok@ejemplo.com\",\"nombre\":\"Ana\",\"password\":\"corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("The password must be between 8 and 72 characters"))
                .andExpect(jsonPath("$.campos.password").value("The password must be between 8 and 72 characters"));
    }

    @Test
    void preflightAceptaAcceptLanguage() throws Exception {
        mvc.perform(options("/api/segmentos")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "accept-language,authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Headers", containsStringIgnoringCase("accept-language")));
    }
}
