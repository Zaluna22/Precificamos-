package com.precificamos.segmento;

import java.math.BigDecimal;

/** Datos comunes reutilizados en las pruebas de estrategias. */
final class Fixtures {
    private Fixtures() {}

    static BigDecimal bd(String v) { return new BigDecimal(v); }

    /** Fijos 1000/mes, 10 % de tasas, 100 ventas del ítem, 500 unidades del negocio (prorrateo = 2), hora = 30. */
    static ComunesResueltos comunes() {
        return new ComunesResueltos(bd("1000"), bd("0.10"), 100, 500, bd("30"));
    }

    /** Ejecuta una estrategia completa como lo hace el servicio. */
    static <T> Resultado calcular(EstrategiaPrecificacion<T> est, T entrada, ComunesResueltos c) {
        CostoVariable costo = est.costoVariable(entrada, c);
        PropuestaPrecio p = est.proponerPrecio(entrada, costo, c);
        return new Resultado(costo, p, CalculadoraBase.analizar(p.precio(), costo.total(), c));
    }

    record Resultado(CostoVariable costo, PropuestaPrecio propuesta, CalculadoraBase.Analisis analisis) {
        BigDecimal precio() { return CalculadoraBase.r(propuesta.precio()); }
        BigDecimal extra(String clave) {
            return propuesta.extras().stream().filter(l -> l.clave().equals(clave)).findFirst().orElseThrow().valor();
        }
        BigDecimal desglose(String clave) {
            return costo.desglose().stream().filter(l -> l.clave().equals(clave)).findFirst().orElseThrow().valor();
        }
    }
}
