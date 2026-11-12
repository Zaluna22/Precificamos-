package com.precificamos.segmento;

import com.precificamos.service.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static com.precificamos.segmento.CalculadoraBase.r;
import static com.precificamos.segmento.Fixtures.bd;
import static org.junit.jupiter.api.Assertions.*;

class UnidadMedidaTest {

    @Test
    void convierteKilosAGramos() {
        assertEquals(0, bd("500").compareTo(UnidadMedida.KG.convertir(bd("0.5"), UnidadMedida.G, "Harina")));
    }

    @Test
    void costoProporcionalDeTelaEnCentimetros() {
        // 30 por 1 m, uso 80 cm → 24
        assertEquals(bd("24.00"), r(UnidadMedida.M.costoProporcional(bd("30"), bd("1"), bd("80"), UnidadMedida.CM, "Tela")));
    }

    @Test
    void docenaAUnidades() {
        assertEquals(bd("4.00"), r(UnidadMedida.DOCENA.costoProporcional(bd("12"), bd("1"), bd("4"), UnidadMedida.UNIDAD, "Huevos")));
    }

    @Test
    void noMezclaDimensiones() {
        assertThrows(RegraDeNegocioException.class, () -> UnidadMedida.KG.convertir(bd("1"), UnidadMedida.ML, "Leche"));
    }

    @Test
    void aceptaMinusculas() {
        assertEquals(UnidadMedida.KG, UnidadMedida.desde("kg"));
    }
}
