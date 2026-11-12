package com.precificamos.segmento;

import com.precificamos.segmento.dto.EntradaVentaMasiva;
import com.precificamos.segmento.estrategia.VentaMasivaEstrategia;
import com.precificamos.segmento.estrategia.VentaMasivaEstrategiaTestAcceso;
import com.precificamos.service.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static com.precificamos.segmento.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class VentaMasivaEstrategiaTest {
    private final VentaMasivaEstrategia est = new VentaMasivaEstrategia();
    /** Supermercado: 10.000 unidades por mes → prorrateo de 0,10 por unidad. */
    private final ComunesResueltos c = new ComunesResueltos(bd("1000"), bd("0.10"), 100, 10000, BigDecimal.ZERO);

    private EntradaVentaMasiva gaseosa(Map<String, BigDecimal> margenes, Integer lleva, Integer paga) {
        return new EntradaVentaMasiva("Gaseosa 2 l", "bebidas", bd("3"), bd("2"), null, margenes, true, lleva, paga);
    }

    @Test
    void usaMargenDeLaCategoriaYRedondeaA99() {
        Fixtures.Resultado r = calcular(est, gaseosa(null, null, null), c);
        assertEquals(bd("25.00"), r.extra("margenCategoriaPct"));
        assertEquals(bd("4.86"), r.extra("precioSinRedondeo")); // 3,16 / (1 - 0,10 - 0,25)
        assertEquals(bd("4.99"), r.precio());
    }

    @Test
    void margenPorCategoriaConfigurable() {
        Fixtures.Resultado r = calcular(est, gaseosa(Map.of("BEBIDAS", bd("30")), null, null), c);
        assertEquals(bd("5.99"), r.precio()); // 3,16 / 0,6 = 5,27 → 5,99
    }

    @Test
    void promoLleve3Pague2DaPerdidaYAvisa() {
        Fixtures.Resultado r = calcular(est, gaseosa(null, 3, 2), c);
        assertEquals(bd("3.33"), r.extra("precioUnitarioPromo"));
        assertTrue(r.extra("lucroPromoUnidad").signum() < 0);
        assertFalse(r.propuesta().avisos().isEmpty());
    }

    @Test
    void promoInvalida() {
        assertThrows(RegraDeNegocioException.class, () -> calcular(est, gaseosa(null, 2, 3), c));
    }

    @Test
    void redondeoPsicologico() {
        assertEquals(bd("4.99"), VentaMasivaEstrategiaTestAcceso.redondear(bd("4.32")));
        assertEquals(bd("4.99"), VentaMasivaEstrategiaTestAcceso.redondear(bd("4.99")));
        assertEquals(bd("5.99"), VentaMasivaEstrategiaTestAcceso.redondear(bd("5.00")));
        assertEquals(bd("5.99"), VentaMasivaEstrategiaTestAcceso.redondear(bd("4.991")));
    }
}
