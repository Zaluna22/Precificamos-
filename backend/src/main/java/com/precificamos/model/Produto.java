package com.precificamos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Produto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;

    @ElementCollection
    @CollectionTable(name = "produto_material", joinColumns = @JoinColumn(name = "produto_id"))
    private List<Material> materiais = new ArrayList<>();

    /** Embalagem, etiqueta, frete por unidade. */
    private BigDecimal outrosCustosVariaveis = BigDecimal.ZERO;
    private BigDecimal horasMaoDeObra = BigDecimal.ZERO;
    private Integer vendasMensaisPrevistas = 1;

    public BigDecimal custoMateriais() {
        return materiais.stream().map(Material::custo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
