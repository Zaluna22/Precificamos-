package com.precificamos.segmento.estrategia;

import java.math.BigDecimal;

/** Da acceso a las pruebas al método de redondeo, que es de paquete. */
public final class VentaMasivaEstrategiaTestAcceso {
    private VentaMasivaEstrategiaTestAcceso() {}

    public static BigDecimal redondear(BigDecimal precio) { return VentaMasivaEstrategia.redondearA99(precio); }
}
