package com.precificamos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Costo fijo mensual del negocio (alquiler, internet, contador, DAS del MEI, etc.). */
@Entity
public class CustoFixo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private BigDecimal valorMensal = BigDecimal.ZERO;

    public CustoFixo() {}
    public CustoFixo(String nome, BigDecimal valorMensal) { this.nome = nome; this.valorMensal = valorMensal; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getValorMensal() { return valorMensal; }
    public void setValorMensal(BigDecimal valorMensal) { this.valorMensal = valorMensal; }
}
