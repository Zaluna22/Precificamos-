package com.precificamos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/** Costo fijo mensual del negocio (alquiler, internet, contador, DAS del MEI, etc.). */
@Entity
@Table(indexes = @Index(name = "idx_custo_fixo_usuario", columnList = "usuario_id"))
public class CustoFixo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private BigDecimal valorMensal = BigDecimal.ZERO;
    /**
     * Dueño del costo. Es nullable para que la app arranque sobre tablas existentes con filas
     * anteriores al login: esas filas quedan sin dueño y no las ve nadie.
     */
    @JsonIgnore
    @Column(name = "usuario_id")
    private Long usuarioId;

    public CustoFixo() {}
    public CustoFixo(String nome, BigDecimal valorMensal) { this.nome = nome; this.valorMensal = valorMensal; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public BigDecimal getValorMensal() { return valorMensal; }
    public void setValorMensal(BigDecimal valorMensal) { this.valorMensal = valorMensal; }
}
