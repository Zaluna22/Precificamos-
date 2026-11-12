package com.precificamos.segmento;

import com.precificamos.service.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static com.precificamos.segmento.CalculadoraBase.*;
import static com.precificamos.segmento.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraBaseTest {

    @Test
    void prorrateaCostosFijosPorUnidadesDelNegocio() {
        assertEquals(bd("2.00"), r(rateo(comunes())));
    }

    @Test
    void precioPorMargenDescuentaTasasYMargen() {
        // 90 / (1 - 0,10 - 0,30) = 150
        assertEquals(bd("150.00"), r(precioPorMargen(bd("90"), bd("0.10"), bd("30"))));
    }

    @Test
    void precioPorMarkupAgregaTasasEncima() {
        // 60 / 0,9 × 2 = 133,33
        assertEquals(bd("133.33"), r(precioPorMarkup(bd("60"), bd("0.10"), bd("2"))));
    }

    @Test
    void rechazaMargenImposible() {
        assertThrows(RegraDeNegocioException.class, () -> precioPorMargen(bd("10"), bd("0.10"), bd("90")));
    }

    @Test
    void puntoDeEquilibrioRedondeaHaciaArriba() {
        // contribución = 150 - 15 - 88 = 47 → 1000 / 47 = 21,3 → 22
        Analisis a = analizar(bd("150"), bd("88"), comunes());
        assertEquals(22, a.puntoEquilibrioUnidades());
        assertEquals(bd("3300.00"), r(a.puntoEquilibrioFacturacion()));
        assertEquals(bd("30.00"), r(a.margenPct()));
    }

    @Test
    void sinContribucionNoHayPuntoDeEquilibrio() {
        assertNull(analizar(bd("10"), bd("12"), comunes()).puntoEquilibrioUnidades());
    }

    @Test
    void factorDeMermaRechazaCienPorCiento() {
        assertThrows(RegraDeNegocioException.class, () -> factorMerma(bd("100"), "La merma"));
    }
}
