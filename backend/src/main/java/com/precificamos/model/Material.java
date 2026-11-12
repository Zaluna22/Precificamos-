package com.precificamos.model;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/** Matéria-prima usada em um produto: preço por kg e quantidade em gramas. */
@Embeddable
public class Material {
    private String nome;
    private BigDecimal precoPorKg = BigDecimal.ZERO;
    private BigDecimal quantidadeGramas = BigDecimal.ZERO;

    public Material() {}

    public Material(String nome, BigDecimal precoPorKg, BigDecimal quantidadeGramas) {
        this.nome = nome;
        this.precoPorKg = precoPorKg;
        this.quantidadeGramas = quantidadeGramas;
    }

    public BigDecimal custo() {
        return precoPorKg.multiply(quantidadeGramas).divide(BigDecimal.valueOf(1000));
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPrecoPorKg() { return precoPorKg; }
    public void setPrecoPorKg(BigDecimal precoPorKg) { this.precoPorKg = precoPorKg; }
    public BigDecimal getQuantidadeGramas() { return quantidadeGramas; }
    public void setQuantidadeGramas(BigDecimal quantidadeGramas) { this.quantidadeGramas = quantidadeGramas; }
}
