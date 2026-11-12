from pydantic import BaseModel, Field

from .pricing import Metodo


class MaterialIn(BaseModel):
    nome: str
    preco_por_kg: float = Field(ge=0)
    quantidade_g: float = Field(ge=0)


class CustoFixoIn(BaseModel):
    nome: str
    valor_mensal: float = Field(ge=0)


class ProdutoIn(BaseModel):
    nome: str
    materiais: list[MaterialIn] = []
    outros_custos_variaveis: float = Field(0, ge=0)
    horas_mao_de_obra: float = Field(0, ge=0)
    vendas_mensais_previstas: int = Field(1, ge=1)


class ConfiguracaoIn(BaseModel):
    custos_fixos: list[CustoFixoIn] = []
    valor_hora: float = Field(0, ge=0)
    imposto_pct: float = Field(0, ge=0, lt=100)
    taxa_cartao_pct: float = Field(0, ge=0, lt=100)
    taxa_marketplace_pct: float = Field(0, ge=0, lt=100)
    comissao_pct: float = Field(0, ge=0, lt=100)
    total_unidades_mes: int = Field(0, ge=0)


class CalculoIn(BaseModel):
    produto: ProdutoIn
    configuracao: ConfiguracaoIn = ConfiguracaoIn()
    metodo: Metodo = Metodo.MARGEM
    valor: float = 30.0


class SimulacaoIn(BaseModel):
    produto: ProdutoIn
    configuracao: ConfiguracaoIn = ConfiguracaoIn()
    preco: float = Field(gt=0)
