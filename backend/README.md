# Precificamos API (backend)

Motor de cálculo e API em FastAPI para definir o preço de venda de um produto a partir de:
materiais, custos variáveis, mão de obra, rateio de custos fixos, impostos e taxas (cartão, marketplace, comissão).

## Rodar
```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload   # docs em http://localhost:8000/docs
pytest -q
```

## Endpoints
- `POST /calcular`: preço sugerido por margem (% sobre o preço) ou markup.
- `POST /simular`: lucro unitário e mensal para um preço escolhido.
- `GET /health`

## Próximos passos
- Persistência (SQLite/PostgreSQL) e login
- Integrar o front-end atual (`index.html`) à API
- Exportar relatório em PDF pelo backend
