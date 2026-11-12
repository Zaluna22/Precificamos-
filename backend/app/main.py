from dataclasses import asdict

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from .pricing import Configuracao, CustoFixo, Material, Produto, calcular_preco, simular
from .schemas import CalculoIn, ConfiguracaoIn, ProdutoIn, SimulacaoIn

app = FastAPI(title="Precificamos API", version="0.1.0",
              description="Calcula o preço de venda ideal para pequenos empreendedores.")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])


def _produto(p: ProdutoIn) -> Produto:
    return Produto(p.nome, [Material(**m.model_dump()) for m in p.materiais],
                   p.outros_custos_variaveis, p.horas_mao_de_obra, p.vendas_mensais_previstas)


def _config(c: ConfiguracaoIn) -> Configuracao:
    d = c.model_dump()
    d["custos_fixos"] = [CustoFixo(**f) for f in d["custos_fixos"]]
    return Configuracao(**d)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/calcular")
def calcular(body: CalculoIn):
    try:
        res = calcular_preco(_produto(body.produto), _config(body.configuracao), body.metodo, body.valor)
    except ValueError as e:
        raise HTTPException(status_code=422, detail=str(e))
    return asdict(res)


@app.post("/simular")
def simular_preco(body: SimulacaoIn):
    try:
        return simular(_produto(body.produto), _config(body.configuracao), body.preco)
    except ValueError as e:
        raise HTTPException(status_code=422, detail=str(e))
