package com.precificamos.segmento;

import com.precificamos.segmento.dto.EntradaFabricacion;
import com.precificamos.segmento.dto.EntradaFabricacion.Insumo;
import com.precificamos.segmento.dto.EntradaFabricacion.MateriaPrima;
import com.precificamos.segmento.estrategia.FabricacionEstrategia;
import com.precificamos.service.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.precificamos.segmento.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class FabricacionEstrategiaTest {
    private final FabricacionEstrategia est = new FabricacionEstrategia();

    /** Remera: 80 cm de tela a 30/m, 4 botones, 1 etiqueta, 45 min de costura, merma 5 %. */
    private EntradaFabricacion remera(UnidadMedida unidadConsumo) {
        return new EntradaFabricacion("Remera",
                List.of(new MateriaPrima("Tela", bd("30"), bd("1"), UnidadMedida.M, bd("80"), unidadConsumo)),
                List.of(new Insumo("Botón", bd("0.20"), bd("4")), new Insumo("Etiqueta", bd("0.50"), bd("1"))),
                bd("45"), null, bd("5"), null, null);
    }

    @Test
    void costoIncluyeMermaInsumosYManoDeObraDesdeValorHora() {
        Fixtures.Resultado r = calcular(est, remera(UnidadMedida.CM), comunes());
        assertEquals(bd("24.00"), r.desglose("materiasPrimas"));
        assertEquals(bd("1.26"), r.desglose("merma"));
        assertEquals(bd("1.30"), r.desglose("insumos"));
        assertEquals(bd("22.50"), r.desglose("manoObra")); // 45 min × (30/60)
    }

    @Test
    void calculaPrecioMinoristaYMayorista() {
        Fixtures.Resultado r = calcular(est, remera(UnidadMedida.CM), comunes());
        assertEquals(bd("100.35"), r.precio());          // 49,06 / (1 - 0,10 - 0,40) + 2 / 0,9
        assertEquals(bd("72.31"), r.extra("precioMayorista")); // 49,06 / (1 - 0,10 - 0,20) + 2 / 0,9
        // El margen de 40 % se aplica a la parte variable del precio (98,13): ganancia 39,25 = 39,11 % del precio
        assertEquals(bd("39.25"), CalculadoraBase.r(r.analisis().lucro()));
        assertEquals(bd("39.11"), CalculadoraBase.r(r.analisis().margenPct()));
    }

    @Test
    void rechazaUnidadesIncompatibles() {
        assertThrows(RegraDeNegocioException.class, () -> calcular(est, remera(UnidadMedida.KG), comunes()));
    }
}
