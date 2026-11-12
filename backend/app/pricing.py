"""Motor de cálculo de preço de venda (Precificamos)."""
from __future__ import annotations

from dataclasses import dataclass, field
from enum import Enum


class Metodo(str, Enum):
    MARGEM = "margem"   # margem de lucro sobre o preço de venda
    MARKUP = "markup"   # multiplicador sobre o custo


@dataclass
class Material:
    nome: str
    preco_por_kg: float
    quantidade_g: float

    @property
    def custo(self) -> float:
        if self.preco_por_kg < 0 or self.quantidade_g < 0:
            raise ValueError(f"Valores negativos no material '{self.nome}'")
        return self.preco_por_kg * self.quantidade_g / 1000


@dataclass
class CustoFixo:
    nome: str
    valor_mensal: float


@dataclass
class Produto:
    nome: str
    materiais: list[Material] = field(default_factory=list)
    outros_custos_variaveis: float = 0.0   # embalagem, etiqueta, frete por unidade
    horas_mao_de_obra: float = 0.0
    vendas_mensais_previstas: int = 1

    @property
    def custo_materiais(self) -> float:
        return sum(m.custo for m in self.materiais)


@dataclass
class Configuracao:
    custos_fixos: list[CustoFixo] = field(default_factory=list)
    valor_hora: float = 0.0
    imposto_pct: float = 0.0          # ex.: Simples Nacional / MEI
    taxa_cartao_pct: float = 0.0
    taxa_marketplace_pct: float = 0.0
    comissao_pct: float = 0.0
    total_unidades_mes: int = 0       # 0 = soma das vendas previstas dos produtos

    @property
    def total_fixo(self) -> float:
        return sum(c.valor_mensal for c in self.custos_fixos)

    @property
    def percentuais_sobre_venda(self) -> float:
        return (self.imposto_pct + self.taxa_cartao_pct
                + self.taxa_marketplace_pct + self.comissao_pct) / 100


@dataclass
class Resultado:
    custo_materiais: float
    custo_variavel: float
    mao_de_obra: float
    rateio_fixo: float
    custo_unitario: float
    preco_sugerido: float
    lucro_unitario: float
    margem_real_pct: float
    ponto_equilibrio_unidades: float | None


def ratear_fixos(config: Configuracao, produto: Produto, total_unidades: int) -> float:
    if total_unidades <= 0:
        raise ValueError("Total de unidades por mês deve ser maior que zero")
    return config.total_fixo / total_unidades


def calcular_preco(produto: Produto, config: Configuracao, metodo: Metodo = Metodo.MARGEM,
                   valor: float = 30.0, total_unidades: int | None = None) -> Resultado:
    """Calcula o preço de venda.

    MARGEM: `valor` é a margem de lucro desejada (% do preço de venda).
    MARKUP: `valor` é o multiplicador aplicado ao custo unitário.
    Impostos e taxas são sempre percentuais sobre o preço de venda.
    """
    total = total_unidades or config.total_unidades_mes or produto.vendas_mensais_previstas
    custo_mat = produto.custo_materiais
    custo_var = custo_mat + produto.outros_custos_variaveis
    mao = produto.horas_mao_de_obra * config.valor_hora
    rateio = ratear_fixos(config, produto, total)
    custo = custo_var + mao + rateio
    taxas = config.percentuais_sobre_venda

    if metodo == Metodo.MARGEM:
        divisor = 1 - taxas - valor / 100
        if divisor <= 0:
            raise ValueError("Margem + impostos/taxas chegam a 100% ou mais do preço")
        preco = custo / divisor
    else:
        if valor <= 0:
            raise ValueError("Markup deve ser positivo")
        if taxas >= 1:
            raise ValueError("Impostos/taxas chegam a 100% ou mais do preço")
        preco = custo * valor / (1 - taxas)

    lucro = preco * (1 - taxas) - custo
    margem = lucro / preco * 100 if preco else 0.0
    contribuicao = preco * (1 - taxas) - custo_var - mao
    pe = config.total_fixo / contribuicao if contribuicao > 0 else None

    r = lambda x: round(x, 2)
    return Resultado(r(custo_mat), r(custo_var), r(mao), r(rateio), r(custo), r(preco),
                     r(lucro), r(margem), None if pe is None else round(pe, 1))


def simular(produto: Produto, config: Configuracao, preco: float,
            total_unidades: int | None = None) -> dict:
    """Mostra o lucro se o produto for vendido a um preço escolhido."""
    total = total_unidades or config.total_unidades_mes or produto.vendas_mensais_previstas
    custo = (produto.custo_materiais + produto.outros_custos_variaveis
             + produto.horas_mao_de_obra * config.valor_hora
             + ratear_fixos(config, produto, total))
    lucro = preco * (1 - config.percentuais_sobre_venda) - custo
    return {"preco": round(preco, 2), "custo_unitario": round(custo, 2),
            "lucro_unitario": round(lucro, 2),
            "margem_pct": round(lucro / preco * 100, 2) if preco else 0.0,
            "lucro_mensal": round(lucro * produto.vendas_mensais_previstas, 2)}


def formula_legada(custo_materiais: float) -> float:
    """Fórmula da versão web original (materiais x 1,4 x 3), mantida para comparação."""
    return round(custo_materiais * 1.4 * 3, 2)
