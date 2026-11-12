package com.precificamos.segmento;

import com.precificamos.segmento.dto.EntradaReventa;
import com.precificamos.segmento.estrategia.ReventaEstrategia;
import org.junit.jupiter.api.Test;

import static com.precificamos.segmento.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class ReventaEstrategiaTest {
    private final ReventaEstrategia est = new ReventaEstrategia();

    private EntradaReventa perfume(int stock) {
        return new EntradaReventa("Perfume", bd("50"), bd("2"), bd("10"), bd("1"), bd("2"), bd("5"), stock);
    }

    @Test
    void costoPuestoEnElLocal() {
        assertEquals(bd("58.00"), CalculadoraBase.r(calcular(est, perfume(0), comunes()).costo().total()));
    }

    @Test
    void precioPorMarkupYLiquidacion() {
        Fixtures.Resultado r = calcular(est, perfume(0), comunes());
        assertEquals(bd("133.33"), r.precio());                   // 60 / 0,9 × 2
        assertEquals(bd("68.24"), r.extra("precioLiquidacion"));  // 58 / (1 - 0,10 - 0,05)
    }

    @Test
    void stockLentoGeneraAviso() {
        Fixtures.Resultado r = calcular(est, perfume(400), comunes());
        assertEquals(bd("120"), r.extra("diasStock"));
        assertEquals(bd("23200.00"), r.extra("capitalInmovilizado"));
        assertEquals(1, r.propuesta().avisos().size());
    }
}
