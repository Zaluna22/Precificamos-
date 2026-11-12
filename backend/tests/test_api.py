from fastapi.testclient import TestClient

from app.main import app

c = TestClient(app)
body = {"produto": {"nome": "Lacinho", "materiais": [{"nome": "Fita", "preco_por_kg": 50, "quantidade_g": 20}],
                    "vendas_mensais_previstas": 50},
        "configuracao": {"custos_fixos": [{"nome": "Aluguel", "valor_mensal": 100}], "imposto_pct": 6}}


def test_calcular():
    r = c.post("/calcular", json=body)
    assert r.status_code == 200 and r.json()["preco_sugerido"] > 0


def test_erro_margem():
    r = c.post("/calcular", json={**body, "valor": 99})
    assert r.status_code == 422


def test_simular():
    r = c.post("/simular", json={**body, "preco": 10})
    assert r.status_code == 200 and "lucro_mensal" in r.json()
