package com.precificamos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Parámetros generales del negocio: valor de la hora de trabajo y porcentajes que se
 * descuentan sobre el precio de venta (impuestos, tarjeta, marketplace y comisión).
 *
 * <p>Hay un registro por usuario y su clave es el id del usuario. Se guarda en la tabla
 * {@code configuracao_usuario}: la tabla anterior ({@code configuracao_negocio}, con un único
 * registro global) queda intacta y ya no se usa, así nadie hereda la configuración vieja.</p>
 */
@Entity
@Table(name = "configuracao_usuario")
public class ConfiguracaoNegocio {
    @Id
    @JsonIgnore
    @Column(name = "usuario_id")
    private Long usuarioId;
    private BigDecimal valorHora = BigDecimal.ZERO;
    private BigDecimal impostoPct = BigDecimal.ZERO;
    private BigDecimal taxaCartaoPct = BigDecimal.ZERO;
    private BigDecimal taxaMarketplacePct = BigDecimal.ZERO;
    private BigDecimal comissaoPct = BigDecimal.ZERO;
    /** Unidades vendidas por mes en todo el negocio. 0 = usa la suma de las ventas previstas de los productos. */
    private Integer totalUnidadesMes = 0;

    /** Suma de los porcentajes sobre la venta, expresada como fracción (ej.: 0,10 = 10 %). */
    public BigDecimal percentuaisSobreVenda() {
        return impostoPct.add(taxaCartaoPct).add(taxaMarketplacePct).add(comissaoPct)
                .divide(BigDecimal.valueOf(100));
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public BigDecimal getValorHora() { return valorHora; }
    public void setValorHora(BigDecimal v) { this.valorHora = v; }
    public BigDecimal getImpostoPct() { return impostoPct; }
    public void setImpostoPct(BigDecimal v) { this.impostoPct = v; }
    public BigDecimal getTaxaCartaoPct() { return taxaCartaoPct; }
    public void setTaxaCartaoPct(BigDecimal v) { this.taxaCartaoPct = v; }
    public BigDecimal getTaxaMarketplacePct() { return taxaMarketplacePct; }
    public void setTaxaMarketplacePct(BigDecimal v) { this.taxaMarketplacePct = v; }
    public BigDecimal getComissaoPct() { return comissaoPct; }
    public void setComissaoPct(BigDecimal v) { this.comissaoPct = v; }
    public Integer getTotalUnidadesMes() { return totalUnidadesMes; }
    public void setTotalUnidadesMes(Integer v) { this.totalUnidadesMes = v; }
}
