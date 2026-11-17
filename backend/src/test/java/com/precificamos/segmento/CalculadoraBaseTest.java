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
    void markupNoMultiplicaLosCostosFijos() {
        // (20 × 2 + 200) / 1 = 240 (antes: (20 + 200) × 2 = 440)
        assertEquals(bd("240.00"), r(precioPorMarkup(bd("20"), bd("200"), bd("0"), bd("2"))));
        // (60 × 2 + 2) / 0,9 = 135,56
        assertEquals(bd("135.56"), r(precioPorMarkup(bd("60"), bd("2"), bd("0.10"), bd("2"))));
    }

    @Test
    void margenSeAplicaSoloAlCostoVariable() {
        // 88 / (1 - 0,10 - 0,30) + 2 / 0,9 = 146,67 + 2,22 = 148,89
        assertEquals(bd("148.89"), r(precioPorMargen(bd("88"), bd("2"), bd("0.10"), bd("30"))));
        // La ganancia por unidad es el margen sobre la parte variable del precio: 0,30 × 146,67 = 44
        Analisis a = analizar(precioPorMargen(bd("88"), bd("2"), bd("0.10"), bd("30")), bd("88"), comunes());
        assertEquals(bd("44.00"), r(a.lucro()));
    }

    @Test
    void puntoDeEquilibrioYGananciaMensualUsanLaMismaParteDeFijos() {
        // Parte de fijos del producto = 2 × 100 = 200; contribución = 150 - 15 - 88 = 47 → 200 / 47 = 4,3 → 5
        Analisis a = analizar(bd("150"), bd("88"), comunes());
        assertEquals(5, a.puntoEquilibrioUnidades());
        assertEquals(bd("4500.00"), r(a.lucroMensual())); // (47 - 2) × 100
        // Vendiendo exactamente el punto de equilibrio la ganancia mensual no es negativa, y una unidad menos sí.
        ComunesResueltos enPe = new ComunesResueltos(bd("1000"), bd("0.10"), 5, 25, bd("30"));
        ComunesResueltos debajo = new ComunesResueltos(bd("1000"), bd("0.10"), 4, 20, bd("30"));
        assertTrue(analizar(bd("150"), bd("88"), enPe).lucroMensual().signum() >= 0);
        assertTrue(analizar(bd("150"), bd("88"), debajo).lucroMensual().signum() < 0);
    }

    @Test
    void sinTotalDelNegocioElPuntoDeEquilibrioUsaTodosLosFijos() {
        ComunesResueltos solo = new ComunesResueltos(bd("1000"), bd("0.10"), 100, 100, bd("30"), false);
        assertEquals(22, analizar(bd("150"), bd("88"), solo).puntoEquilibrioUnidades()); // 1000 / 47
    }

    @Test
    void avisaSoloSiFaltaElTotalYLosFijosSuperanAlCostoVariable() {
        ComunesResueltos sinTotal = new ComunesResueltos(bd("600"), bd("0"), 3, 3, bd("0"), false);
        ComunesResueltos conTotal = new ComunesResueltos(bd("600"), bd("0"), 3, 3, bd("0"), true);
        assertTrue(fijosConcentradosEnElProducto(sinTotal, bd("20")));   // 200 > 20
        assertFalse(fijosConcentradosEnElProducto(sinTotal, bd("250"))); // 200 < 250
        assertFalse(fijosConcentradosEnElProducto(conTotal, bd("20")));
    }

    @Test
    void rechazaMargenImposible() {
        assertThrows(RegraDeNegocioException.class, () -> precioPorMargen(bd("10"), bd("0.10"), bd("90")));
    }

    @Test
    void puntoDeEquilibrioRedondeaHaciaArriba() {
        // contribución = 150 - 15 - 88 = 47 → parte de fijos 200 / 47 = 4,3 → 5
        Analisis a = analizar(bd("150"), bd("88"), comunes());
        assertEquals(5, a.puntoEquilibrioUnidades());
        assertEquals(bd("750.00"), r(a.puntoEquilibrioFacturacion()));
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
