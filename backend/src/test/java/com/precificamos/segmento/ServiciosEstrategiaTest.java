package com.precificamos.segmento;

import com.precificamos.segmento.dto.EntradaServicios;
import com.precificamos.segmento.dto.EntradaServicios.Material;
import com.precificamos.segmento.estrategia.ServiciosEstrategia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.precificamos.segmento.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class ServiciosEstrategiaTest {
    private final ServiciosEstrategia est = new ServiciosEstrategia();

    /** Coloración a domicilio: 1,5 h, tintura 18, 20 km a 1/km + 5 de estacionamiento, margen 30 %. */
    private EntradaServicios coloracion(BigDecimal valorHora) {
        return new EntradaServicios("Coloración", bd("1.5"), valorHora, List.of(new Material("Tintura", bd("18"))),
                bd("20"), bd("1"), bd("5"), bd("30"));
    }

    @Test
    void costoConTiempoMaterialesYDesplazamiento() {
        Fixtures.Resultado r = calcular(est, coloracion(null), comunes());
        assertEquals(bd("45.00"), r.desglose("tiempo")); // 1,5 h × 30 de la configuración
        assertEquals(bd("25.00"), r.desglose("desplazamiento"));
        assertEquals(bd("88.00"), CalculadoraBase.r(r.costo().total()));
    }

    @Test
    void precioPorMargenYPuntoDeEquilibrio() {
        Fixtures.Resultado r = calcular(est, coloracion(null), comunes());
        assertEquals(bd("148.89"), r.precio());          // 88 / (1 - 0,10 - 0,30) + 2 / 0,9
        assertEquals(bd("99.26"), r.extra("precioPorHora"));
        assertEquals(5, r.analisis().puntoEquilibrioUnidades()); // parte de fijos 200 / contribución 46
    }

    @Test
    void valorHoraPropioTienePrioridad() {
        assertEquals(bd("60.00"), calcular(est, coloracion(bd("40")), comunes()).desglose("tiempo"));
    }

    @Test
    void avisaSiLaHoraEsCero() {
        ComunesResueltos sinHora = new ComunesResueltos(bd("0"), bd("0"), 10, 10, BigDecimal.ZERO);
        assertFalse(calcular(est, coloracion(null), sinHora).propuesta().avisos().isEmpty());
    }
}
