import pytest

from app.pricing import (Configuracao, CustoFixo, Material, Metodo, Produto,
                         calcular_preco, formula_legada, simular)


def produto():
    return Produto("Lacinho", [Material("Fita", 50, 20), Material("Cola", 30, 5)],
                   outros_custos_variaveis=0.5, horas_mao_de_obra=0.25, vendas_mensais_previstas=100)


def config():
    return Configuracao([CustoFixo("Aluguel", 300), CustoFixo("Internet", 100)],
                        valor_hora=20, imposto_pct=6, taxa_cartao_pct=4)


def test_custo_materiais():
    assert produto().custo_materiais == pytest.approx(1.15)


def test_margem():
    r = calcular_preco(produto(), config(), Metodo.MARGEM, 30)
    assert r.custo_unitario == pytest.approx(10.65)
    assert r.preco_sugerido == pytest.approx(17.75)
    assert r.margem_real_pct == pytest.approx(30, abs=0.1)


def test_markup():
    r = calcular_preco(produto(), config(), Metodo.MARKUP, 2)
    assert r.preco_sugerido == pytest.approx(10.65 * 2 / 0.9, abs=0.01)


def test_ponto_equilibrio():
    r = calcular_preco(produto(), config(), Metodo.MARGEM, 30)
    assert r.ponto_equilibrio_unidades and r.ponto_equilibrio_unidades < 100


def test_margem_impossivel():
    with pytest.raises(ValueError):
        calcular_preco(produto(), config(), Metodo.MARGEM, 95)


def test_simular_prejuizo():
    s = simular(produto(), config(), 8)
    assert s["lucro_unitario"] < 0


def test_formula_legada():
    assert formula_legada(10) == 42.0
