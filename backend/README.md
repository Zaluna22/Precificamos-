# Precificamos API (Java + Spring Boot + MySQL)

API que calcula o preço de venda ideal a partir de materiais, custos variáveis, mão de obra,
rateio de custos fixos, impostos e taxas (cartão, marketplace, comissão).

## Rodar
```bash
docker compose up --build          # MySQL + API em http://localhost:8080
# ou, com MySQL local:
cd backend && mvn spring-boot:run
mvn test                           # testes usam H2 em memória
```

## Endpoints
| Método | Rota | O que faz |
|---|---|---|
| GET/POST | `/api/produtos` | lista e cria produtos com materiais |
| GET/PUT/DELETE | `/api/produtos/{id}` | detalhe, edição e remoção |
| GET | `/api/produtos/{id}/preco?metodo=MARGEM&valor=30` | preço sugerido (margem % ou markup) |
| GET | `/api/produtos/{id}/simulacao?preco=20` | lucro unitário e mensal a um preço |
| GET/POST/DELETE | `/api/custos-fixos` | custos fixos mensais |
| GET/PUT | `/api/configuracao` | valor da hora, impostos e taxas |

## Roadmap
- Front-end em Angular consumindo a API
- Login com Spring Security + JWT e dados por usuário
- Relatórios em PDF e histórico de preços
- Comparação com preços de concorrentes
