package com.precificamos.meusprodutos;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Producto que el usuario guardó desde la calculadora ("Meus produtos").
 *
 * <p>Guarda la categoría, los datos cargados en el formulario tal cual se mandaron a
 * {@code /api/segmentos/{codigo}/calcular} (JSON en {@code comun} y {@code datos}), una foto del
 * resultado en ese momento y el precio final que eligió el usuario. Con {@code ddl-auto=update}
 * Hibernate crea la tabla {@code produto_salvo} sola la primera vez.</p>
 */
@Entity
@Table(name = "produto_salvo", indexes = @Index(name = "idx_produto_salvo_usuario", columnList = "usuario_id"))
public class ProdutoSalvo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /** Código de la categoría (REVENTA, FABRICACION…). */
    @Column(nullable = false, length = 40)
    private String segmento;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** Parámetros comunes del formulario, en JSON. */
    @Lob
    @Column(name = "comun_json")
    private String comunJson;

    /** Datos propios de la categoría, en JSON. */
    @Lob
    @Column(name = "datos_json")
    private String datosJson;

    @Column(precision = 19, scale = 4) private BigDecimal precioSugerido;
    @Column(precision = 19, scale = 4) private BigDecimal costoTotalUnitario;
    @Column(precision = 19, scale = 4) private BigDecimal margenRealPct;
    @Column(precision = 19, scale = 4) private BigDecimal lucroUnitario;
    /** Precio con el que el usuario decidió vender (por defecto, el sugerido). */
    @Column(precision = 19, scale = 4) private BigDecimal precioFinal;

    @Column(nullable = false) private Instant creadoEn;
    @Column(nullable = false) private Instant actualizadoEn;

    @PrePersist
    void alCrear() {
        creadoEn = Instant.now();
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getComunJson() { return comunJson; }
    public void setComunJson(String comunJson) { this.comunJson = comunJson; }
    public String getDatosJson() { return datosJson; }
    public void setDatosJson(String datosJson) { this.datosJson = datosJson; }
    public BigDecimal getPrecioSugerido() { return precioSugerido; }
    public void setPrecioSugerido(BigDecimal v) { this.precioSugerido = v; }
    public BigDecimal getCostoTotalUnitario() { return costoTotalUnitario; }
    public void setCostoTotalUnitario(BigDecimal v) { this.costoTotalUnitario = v; }
    public BigDecimal getMargenRealPct() { return margenRealPct; }
    public void setMargenRealPct(BigDecimal v) { this.margenRealPct = v; }
    public BigDecimal getLucroUnitario() { return lucroUnitario; }
    public void setLucroUnitario(BigDecimal v) { this.lucroUnitario = v; }
    public BigDecimal getPrecioFinal() { return precioFinal; }
    public void setPrecioFinal(BigDecimal v) { this.precioFinal = v; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
}
