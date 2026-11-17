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
        assertEquals(bd("131.11"), r.precio());                   // (58 × 2 + 2) / 0,9
        assertEquals(bd("68.24"), r.extra("precioLiquidacion"));  // 58 / (1 - 0,10 - 0,05)
    }

    /** Ejemplo del dueño: compra 20, markup 2, fijos 600, 3 ventas por mes, sin otros datos. */
    private static EntradaReventa simple() {
        return new EntradaReventa("Simple", bd("20"), null, null, null, bd("2"), null, null);
    }

    @Test
    void costosFijosSeSumanAlCostoSinMarkup() {
        // Sin total del negocio: rateo = 600 / 3 = 200 → 20 × 2 + 200 = 240
        ComunesResueltos sinTotal = new ComunesResueltos(bd("600"), bd("0"), 3, 3, bd("0"), false);
        assertEquals(bd("240.00"), calcular(est, simple(), sinTotal).precio());
        // Con 150 unidades del negocio: rateo = 4 → 20 × 2 + 4 = 44
        ComunesResueltos conTotal = new ComunesResueltos(bd("600"), bd("0"), 3, 150, bd("0"));
        assertEquals(bd("44.00"), calcular(est, simple(), conTotal).precio());
        // Con tarjeta 3,5 %: 44 / 0,965 = 45,60
        ComunesResueltos conTarjeta = new ComunesResueltos(bd("600"), bd("0.035"), 3, 150, bd("0"));
        assertEquals(bd("45.60"), calcular(est, simple(), conTarjeta).precio());
    }

    @Test
    void stockLentoGeneraAviso() {
        Fixtures.Resultado r = calcular(est, perfume(400), comunes());
        assertEquals(bd("120"), r.extra("diasStock"));
        assertEquals(bd("23200.00"), r.extra("capitalInmovilizado"));
        assertEquals(1, r.propuesta().avisos().size());
    }
}
