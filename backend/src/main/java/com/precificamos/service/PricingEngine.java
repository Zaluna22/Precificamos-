package com.precificamos.service;

import com.precificamos.dto.ResultadoPreco;
import com.precificamos.dto.ResultadoSimulacao;
import com.precificamos.model.ConfiguracaoNegocio;
import com.precificamos.model.MetodoPrecificacao;
import com.precificamos.model.Produto;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Cálculo puro del precio de venta de un {@link Produto}, sin dependencia de base de datos. */
public class PricingEngine {
    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    /**
     * Calcula el precio sugerido por margen (% sobre el precio) o por markup (multiplicador del costo),
     * junto con el desglose de costos, la margen real y el punto de equilibrio en unidades.
     */
    public ResultadoPreco calcular(Produto p, ConfiguracaoNegocio c, BigDecimal totalFixo, int totalUnidades,
                                   MetodoPrecificacao metodo, BigDecimal valor) {
        if (totalUnidades <= 0) throw new RegraDeNegocioException("El total de unidades por mes debe ser mayor que cero");
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
                throw new RegraDeNegocioException("Margen + impuestos/tasas llegan al 100 % o más del precio");
            preco = custo.divide(divisor, MC);
        } else {
            if (valor.signum() <= 0) throw new RegraDeNegocioException("El markup debe ser positivo");
            if (liquido.signum() <= 0) throw new RegraDeNegocioException("Impuestos/tasas llegan al 100 % o más");
            preco = custo.multiply(valor).divide(liquido, MC);
        }

        BigDecimal lucro = preco.multiply(liquido).subtract(custo);
        BigDecimal margem = preco.signum() == 0 ? BigDecimal.ZERO : lucro.divide(preco, MC).multiply(CEM);
        BigDecimal contribuicao = preco.multiply(liquido).subtract(custoVar).subtract(mao);
        BigDecimal pe = contribuicao.signum() > 0 ? totalFixo.divide(contribuicao, 1, RoundingMode.HALF_UP) : null;

        return new ResultadoPreco(r(custoMat), r(custoVar), r(mao), r(rateio), r(custo), r(preco), r(lucro), r(margem), pe);
    }

    /** Simula la ganancia unitaria y mensual si el producto se vende a un precio dado. */
    public ResultadoSimulacao simular(Produto p, ConfiguracaoNegocio c, BigDecimal totalFixo, int totalUnidades,
                                      BigDecimal preco) {
        if (totalUnidades <= 0) throw new RegraDeNegocioException("El total de unidades por mes debe ser mayor que cero");
        if (preco.signum() <= 0) throw new RegraDeNegocioException("El precio debe ser positivo");
        BigDecimal custo = p.custoMateriais().add(p.getOutrosCustosVariaveis())
                .add(p.getHorasMaoDeObra().multiply(c.getValorHora()))
                .add(totalFixo.divide(BigDecimal.valueOf(totalUnidades), MC));
        BigDecimal lucro = preco.multiply(BigDecimal.ONE.subtract(c.percentuaisSobreVenda())).subtract(custo);
        BigDecimal margem = lucro.divide(preco, MC).multiply(CEM);
        BigDecimal mensal = lucro.multiply(BigDecimal.valueOf(p.getVendasMensaisPrevistas()));
        return new ResultadoSimulacao(r(preco), r(custo), r(lucro), r(margem), r(mensal));
    }

    /** Fórmula de la versión web original (materiales x 1,4 x 3), mantenida para comparación. */
    public BigDecimal formulaLegada(BigDecimal custoMateriais) {
        return r(custoMateriais.multiply(BigDecimal.valueOf(1.4)).multiply(BigDecimal.valueOf(3)));
    }

    private static BigDecimal r(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }
}
