package com.precificamos.service;

import com.precificamos.dto.ResultadoPreco;
import com.precificamos.dto.ResultadoSimulacao;
import com.precificamos.model.*;
import com.precificamos.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

    public ConfiguracaoNegocio configuracao() {
        return configuracoes.findById(1L).orElseGet(ConfiguracaoNegocio::new);
    }

    public ConfiguracaoNegocio salvarConfiguracao(ConfiguracaoNegocio c) {
        c.setId(1L);
        return configuracoes.save(c);
    }

    public ResultadoPreco precoDoProduto(Long id, MetodoPrecificacao metodo, BigDecimal valor) {
        Produto p = buscar(id);
        ConfiguracaoNegocio c = configuracao();
        return engine.calcular(p, c, totalFixo(), totalUnidades(c), metodo, valor);
    }

    public ResultadoSimulacao simular(Long id, BigDecimal preco) {
        ConfiguracaoNegocio c = configuracao();
        return engine.simular(buscar(id), c, totalFixo(), totalUnidades(c), preco);
    }

    public Produto buscar(Long id) {
        return produtos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto " + id + " não encontrado"));
    }

    private BigDecimal totalFixo() {
        return custosFixos.findAll().stream().map(CustoFixo::getValorMensal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int totalUnidades(ConfiguracaoNegocio c) {
        if (c.getTotalUnidadesMes() != null && c.getTotalUnidadesMes() > 0) return c.getTotalUnidadesMes();
        return produtos.findAll().stream().mapToInt(Produto::getVendasMensaisPrevistas).sum();
    }
}
