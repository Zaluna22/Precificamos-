package com.precificamos.service;

import com.precificamos.dto.ResultadoPreco;
import com.precificamos.dto.ResultadoSimulacao;
import com.precificamos.model.*;
import com.precificamos.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Orquesta el cálculo de precios de productos guardados usando la configuración y los costos fijos.
 * Todos los métodos reciben el id del usuario dueño; {@code null} significa anónimo
 * (configuración vacía y sin costos fijos).
 */
@Service
public class PrecificacaoService {
    private final ProdutoRepository produtos;
    private final CustoFixoRepository custosFixos;
    private final ConfiguracaoNegocioRepository configuracoes;
    private final PricingEngine engine = new PricingEngine();

    public PrecificacaoService(ProdutoRepository produtos, CustoFixoRepository custosFixos,
                               ConfiguracaoNegocioRepository configuracoes) {
        this.produtos = produtos;
        this.custosFixos = custosFixos;
        this.configuracoes = configuracoes;
    }

    /** Configuración del usuario, o una vacía si es anónimo o todavía no guardó nada. */
    public ConfiguracaoNegocio configuracao(Long usuarioId) {
        if (usuarioId == null) return new ConfiguracaoNegocio();
        return configuracoes.findById(usuarioId).orElseGet(ConfiguracaoNegocio::new);
    }

    public ConfiguracaoNegocio salvarConfiguracao(Long usuarioId, ConfiguracaoNegocio c) {
        c.setUsuarioId(usuarioId);
        return configuracoes.save(c);
    }

    public ResultadoPreco precoDoProduto(Long usuarioId, Long id, MetodoPrecificacao metodo, BigDecimal valor) {
        Produto p = buscar(usuarioId, id);
        ConfiguracaoNegocio c = configuracao(usuarioId);
        return engine.calcular(p, c, totalFixo(usuarioId), totalUnidades(usuarioId, c), metodo, valor);
    }

    public ResultadoSimulacao simular(Long usuarioId, Long id, BigDecimal preco) {
        ConfiguracaoNegocio c = configuracao(usuarioId);
        return engine.simular(buscar(usuarioId, id), c, totalFixo(usuarioId), totalUnidades(usuarioId, c), preco);
    }

    /** Producto del usuario; uno ajeno responde 404 igual que uno inexistente. */
    public Produto buscar(Long usuarioId, Long id) {
        return produtos.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto " + id + " no encontrado"));
    }

    /** Suma de los costos fijos mensuales del usuario (0 si es anónimo). */
    public BigDecimal totalFixo(Long usuarioId) {
        if (usuarioId == null) return BigDecimal.ZERO;
        return custosFixos.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
                .map(CustoFixo::getValorMensal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Unidades mensuales usadas para prorratear los costos fijos. */
    private int totalUnidades(Long usuarioId, ConfiguracaoNegocio c) {
        if (c.getTotalUnidadesMes() != null && c.getTotalUnidadesMes() > 0) return c.getTotalUnidadesMes();
        return produtos.findByUsuarioIdOrderByIdAsc(usuarioId).stream().mapToInt(Produto::getVendasMensaisPrevistas).sum();
    }
}
