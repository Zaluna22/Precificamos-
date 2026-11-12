package com.precificamos.service;

import com.precificamos.dto.ResultadoPreco;
import com.precificamos.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PricingEngineTest {
    private final PricingEngine engine = new PricingEngine();

    private Produto produto() {
        Produto p = new Produto();
        p.setNome("Lacinho");
        p.setMateriais(List.of(new Material("Fita", bd("50"), bd("20")), new Material("Cola", bd("30"), bd("5"))));
        p.setOutrosCustosVariaveis(bd("0.5"));
        p.setHorasMaoDeObra(bd("0.25"));
        p.setVendasMensaisPrevistas(100);
        return p;
    }

    private ConfiguracaoNegocio config() {
        ConfiguracaoNegocio c = new ConfiguracaoNegocio();
        c.setValorHora(bd("20"));
        c.setImpostoPct(bd("6"));
        c.setTaxaCartaoPct(bd("4"));
        return c;
    }

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    @Test
    void calculaCustoDosMateriais() {
        assertEquals(0, bd("1.15").compareTo(produto().custoMateriais()));
    }

    @Test
    void calculaPrecoPorMargem() {
        ResultadoPreco r = engine.calcular(produto(), config(), bd("400"), 100, MetodoPrecificacao.MARGEM, bd("30"));
        assertEquals(bd("10.65"), r.custoUnitario());
        assertEquals(bd("17.75"), r.precoSugerido());
        assertEquals(bd("30.00"), r.margemRealPct());
    }

    @Test
    void calculaPrecoPorMarkup() {
        ResultadoPreco r = engine.calcular(produto(), config(), bd("400"), 100, MetodoPrecificacao.MARKUP, bd("2"));
        assertEquals(bd("23.67"), r.precoSugerido());
    }

    @Test
    void pontoDeEquilibrioAbaixoDasVendasPrevistas() {
        ResultadoPreco r = engine.calcular(produto(), config(), bd("400"), 100, MetodoPrecificacao.MARGEM, bd("30"));
        assertNotNull(r.pontoEquilibrioUnidades());
        assertTrue(r.pontoEquilibrioUnidades().compareTo(bd("100")) < 0);
    }

    @Test
    void rejeitaMargemImpossivel() {
        assertThrows(RegraDeNegocioException.class,
                () -> engine.calcular(produto(), config(), bd("400"), 100, MetodoPrecificacao.MARGEM, bd("95")));
    }

    @Test
    void simulacaoMostraPrejuizo() {
        assertTrue(engine.simular(produto(), config(), bd("400"), 100, bd("8")).lucroUnitario().signum() < 0);
    }

    @Test
    void mantemFormulaLegada() {
        assertEquals(bd("42.00"), engine.formulaLegada(bd("10")));
    }
}
