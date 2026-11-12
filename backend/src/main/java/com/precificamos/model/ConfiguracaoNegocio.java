package com.precificamos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Parâmetros do negócio: valor da hora e percentuais cobrados sobre o preço de venda. */
@Entity
public class ConfiguracaoNegocio {
    @Id
    private Long id = 1L;
    private BigDecimal valorHora = BigDecimal.ZERO;
    private BigDecimal impostoPct = BigDecimal.ZERO;
    private BigDecimal taxaCartaoPct = BigDecimal.ZERO;
    private BigDecimal taxaMarketplacePct = BigDecimal.ZERO;
    private BigDecimal comissaoPct = BigDecimal.ZERO;
    /** 0 = usa a soma das vendas previstas de todos os produtos. */
    private Integer totalUnidadesMes = 0;

    public BigDecimal percentuaisSobreVenda() {
        return impostoPct.add(taxaCartaoPct).add(taxaMarketplacePct).add(comissaoPct)
                .divide(BigDecimal.valueOf(100));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
