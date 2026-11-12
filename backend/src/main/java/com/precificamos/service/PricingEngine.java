package com.precificamos.service;

import com.precificamos.dto.ResultadoPreco;
import com.precificamos.dto.ResultadoSimulacao;
import com.precificamos.model.ConfiguracaoNegocio;
import com.precificamos.model.MetodoPrecificacao;
import com.precificamos.model.Produto;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Cálculo puro do preço de venda, sem dependência de banco. */
public class PricingEngine {
    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    public ResultadoPreco calcular(Produto p, ConfiguracaoNegocio c, BigDecimal totalFixo, int totalUnidades,
                                   MetodoPrecificacao metodo, BigDecimal valor) {
        if (totalUnidades <= 0) throw new RegraDeNegocioException("Total de unidades por mês deve ser maior que zero");
        BigDecimal custoMat = p.custoMateriais();
        BigDecimal custoVar = custoMat.add(p.getOutrosCustosVariaveis());
        BigDecimal mao = p.getHorasMaoDeObra().multiply(c.getValorHora());
        BigDecimal rateio = totalFixo.divide(BigDecimal.valueOf(totalUnidades), MC);
        BigDecimal custo = custoVar.add(mao).add(rateio);
        BigDecimal taxas = c.percentuaisSobreVenda();
        BigDecimal liquido = BigDecimal.ONE.subtract(taxas);

        BigDecimal preco;
        if (metodo == MetodoPrecificacao.MARGEM) {
            BigDecimal divisor = liquido.subtract(valor.divide(CEM, MC));
            if (divisor.signum() <= 0)
                throw new RegraDeNegocioException("Margem + impostos/taxas chegam a 100% ou mais do preço");
            preco = custo.divide(divisor, MC);
        } else {
            if (valor.signum() <= 0) throw new RegraDeNegocioException("Markup deve ser positivo");
            if (liquido.signum() <= 0) throw new RegraDeNegocioException("Impostos/taxas chegam a 100% ou mais");
            preco = custo.multiply(valor).divide(liquido, MC);
        }

        BigDecimal lucro = preco.multiply(liquido).subtract(custo);
        BigDecimal margem = preco.signum() == 0 ? BigDecimal.ZERO : lucro.divide(preco, MC).multiply(CEM);
        BigDecimal contribuicao = preco.multiply(liquido).subtract(custoVar).subtract(mao);
        BigDecimal pe = contribuicao.signum() > 0 ? totalFixo.divide(contribuicao, 1, RoundingMode.HALF_UP) : null;

        return new ResultadoPreco(r(custoMat), r(custoVar), r(mao), r(rateio), r(custo), r(preco), r(lucro), r(margem), pe);
    }

    public ResultadoSimulacao simular(Produto p, ConfiguracaoNegocio c, BigDecimal totalFixo, int totalUnidades,
                                      BigDecimal preco) {
        if (totalUnidades <= 0) throw new RegraDeNegocioException("Total de unidades por mês deve ser maior que zero");
        if (preco.signum() <= 0) throw new RegraDeNegocioException("Preço deve ser positivo");
        BigDecimal custo = p.custoMateriais().add(p.getOutrosCustosVariaveis())
                .add(p.getHorasMaoDeObra().multiply(c.getValorHora()))
                .add(totalFixo.divide(BigDecimal.valueOf(totalUnidades), MC));
        BigDecimal lucro = preco.multiply(BigDecimal.ONE.subtract(c.percentuaisSobreVenda())).subtract(custo);
        BigDecimal margem = lucro.divide(preco, MC).multiply(CEM);
        BigDecimal mensal = lucro.multiply(BigDecimal.valueOf(p.getVendasMensaisPrevistas()));
        return new ResultadoSimulacao(r(preco), r(custo), r(lucro), r(margem), r(mensal));
    }

    /** Fórmula da versão web original (materiais x 1,4 x 3), mantida para comparação. */
    public BigDecimal formulaLegada(BigDecimal custoMateriais) {
        return r(custoMateriais.multiply(BigDecimal.valueOf(1.4)).multiply(BigDecimal.valueOf(3)));
    }

    private static BigDecimal r(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }
}
