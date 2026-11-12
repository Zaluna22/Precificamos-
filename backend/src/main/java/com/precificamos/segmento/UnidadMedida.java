package com.precificamos.segmento;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.precificamos.service.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Unidades de medida para ingredientes y materias primas. Cada unidad pertenece a una
 * dimensión (masa, volumen, longitud, área o unidades) y tiene un factor hacia la unidad base
 * de esa dimensión, lo que permite comprar en kg y usar en gramos, comprar en metros y usar
 * en centímetros, etc.
 */
public enum UnidadMedida {
    G(Dimension.MASA, "1"), KG(Dimension.MASA, "1000"),
    ML(Dimension.VOLUMEN, "1"), L(Dimension.VOLUMEN, "1000"),
    CM(Dimension.LONGITUD, "1"), M(Dimension.LONGITUD, "100"),
    CM2(Dimension.AREA, "1"), M2(Dimension.AREA, "10000"),
    UNIDAD(Dimension.UNIDADES, "1"), DOCENA(Dimension.UNIDADES, "12");

    /** Magnitud física de la unidad; solo se convierte dentro de la misma dimensión. */
    public enum Dimension { MASA, VOLUMEN, LONGITUD, AREA, UNIDADES }

    private final Dimension dimension;
    private final BigDecimal factorBase;

    UnidadMedida(Dimension dimension, String factorBase) {
        this.dimension = dimension;
        this.factorBase = new BigDecimal(factorBase);
    }

    public Dimension getDimension() { return dimension; }

    /** Acepta "kg", "Kg", "KG", "unidad", etc. */
    @JsonCreator
    public static UnidadMedida desde(String valor) {
        if (valor == null) return null;
        try {
            return valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unidad de medida desconocida: " + valor);
        }
    }

    /**
     * Convierte una cantidad de esta unidad a otra de la misma dimensión.
     *
     * @param concepto nombre del ingrediente o material, usado en el mensaje de error
     */
    public BigDecimal convertir(BigDecimal cantidad, UnidadMedida destino, String concepto) {
        if (destino.dimension != dimension) {
            throw new RegraDeNegocioException("No se puede convertir " + name().toLowerCase() + " a "
                    + destino.name().toLowerCase() + " en '" + concepto + "'");
        }
        return cantidad.multiply(factorBase).divide(destino.factorBase, MathContext.DECIMAL64);
    }

    /**
     * Costo de usar {@code cantidadUsada} (en {@code unidadUsada}) de algo que se compró a
     * {@code precioCompra} por {@code cantidadCompra} unidades de esta unidad.
     */
    public BigDecimal costoProporcional(BigDecimal precioCompra, BigDecimal cantidadCompra,
                                        BigDecimal cantidadUsada, UnidadMedida unidadUsada, String concepto) {
        if (cantidadCompra.signum() <= 0) {
            throw new RegraDeNegocioException("La cantidad comprada de '" + concepto + "' debe ser mayor que cero");
        }
        BigDecimal usadaEnUnidadCompra = unidadUsada.convertir(cantidadUsada, this, concepto);
        return precioCompra.multiply(usadaEnUnidadCompra).divide(cantidadCompra, MathContext.DECIMAL64);
    }
}
