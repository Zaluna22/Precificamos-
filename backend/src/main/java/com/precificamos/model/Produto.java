package com.precificamos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Producto genérico con materiales, costos variables y mano de obra (API original de productos). */
@Entity
@Table(indexes = @Index(name = "idx_produto_usuario", columnList = "usuario_id"))
public class Produto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    /** Dueño del producto (nullable por las filas anteriores al login, que no ve nadie). */
    @JsonIgnore
    @Column(name = "usuario_id")
    private Long usuarioId;

    @ElementCollection
    @CollectionTable(name = "produto_material", joinColumns = @JoinColumn(name = "produto_id"))
    private List<Material> materiais = new ArrayList<>();

    /** Embalaje, etiqueta, flete por unidad. */
    private BigDecimal outrosCustosVariaveis = BigDecimal.ZERO;
    private BigDecimal horasMaoDeObra = BigDecimal.ZERO;
    private Integer vendasMensaisPrevistas = 1;

    /** Suma del costo de todos los materiales del producto. */
    public BigDecimal custoMateriais() {
        return materiais.stream().map(Material::custo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public List<Material> getMateriais() { return materiais; }
    public void setMateriais(List<Material> materiais) { this.materiais = materiais; }
    public BigDecimal getOutrosCustosVariaveis() { return outrosCustosVariaveis; }
    public void setOutrosCustosVariaveis(BigDecimal v) { this.outrosCustosVariaveis = v; }
    public BigDecimal getHorasMaoDeObra() { return horasMaoDeObra; }
    public void setHorasMaoDeObra(BigDecimal v) { this.horasMaoDeObra = v; }
    public Integer getVendasMensaisPrevistas() { return vendasMensaisPrevistas; }
    public void setVendasMensaisPrevistas(Integer v) { this.vendasMensaisPrevistas = v; }
}
