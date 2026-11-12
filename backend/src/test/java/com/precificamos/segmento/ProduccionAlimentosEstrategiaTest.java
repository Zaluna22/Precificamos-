package com.precificamos.segmento;

import com.precificamos.segmento.dto.EntradaProduccionAlimentos;
import com.precificamos.segmento.dto.EntradaProduccionAlimentos.Ingrediente;
import com.precificamos.segmento.estrategia.ProduccionAlimentosEstrategia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.precificamos.segmento.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class ProduccionAlimentosEstrategiaTest {
    private final ProduccionAlimentosEstrategia est = new ProduccionAlimentosEstrategia();

    /** Torta de 10 porciones: ingredientes 17,25; merma 10 %; 60 min a 3/h; embalaje 1 por porción. */
    private EntradaProduccionAlimentos torta(String cmv) {
        return new EntradaProduccionAlimentos("Torta de chocolate", List.of(
                new Ingrediente("Harina", bd("6.50"), bd("1"), UnidadMedida.KG, bd("500"), UnidadMedida.G),
                new Ingrediente("Chocolate", bd("20"), bd("200"), UnidadMedida.G, bd("100"), null),
                new Ingrediente("Huevos", bd("12"), bd("1"), UnidadMedida.DOCENA, bd("4"), UnidadMedida.UNIDAD)),
                10, bd("10"), bd("60"), bd("3"), bd("1"), cmv == null ? null : new BigDecimal(cmv));
    }

    @Test
    void calculaCostoPorPorcion() {
        Fixtures.Resultado r = calcular(est, torta("30"), comunes());
        assertEquals(bd("1.73"), r.desglose("ingredientes"));
        assertEquals(bd("0.19"), r.desglose("merma"));
        assertEquals(bd("0.30"), r.desglose("energia"));
        assertEquals(bd("3.22"), CalculadoraBase.r(r.costo().total()));
    }

    @Test
    void precioSaleDelCmvObjetivo() {
        Fixtures.Resultado r = calcular(est, torta(null), comunes()); // CMV por defecto 30 %
        assertEquals(bd("6.39"), r.precio());
        assertEquals(bd("30.00"), r.extra("cmvRealPct"));
        assertEquals(395, r.analisis().puntoEquilibrioUnidades());
        assertTrue(r.propuesta().avisos().isEmpty());
    }

    @Test
    void siElCmvNoCubreCostosUsaPrecioMinimoYAvisa() {
        Fixtures.Resultado r = calcular(est, torta("50"), comunes());
        assertEquals(bd("5.80"), r.precio());
        assertEquals(0, r.analisis().lucro().setScale(2, java.math.RoundingMode.HALF_UP).signum());
        assertFalse(r.propuesta().avisos().isEmpty());
    }
}
