# Precificamos API (Java + Spring Boot + MySQL)

API que calcula el precio de venta ideal por **categoría de negocio** (producción de alimentos,
fabricación, reventa, venta masiva y servicios) y mantiene la API original de productos.

## Ejecutar

```bash
docker compose up --build          # MySQL + API en http://localhost:8080 (desde la raíz)
# o, con un MySQL local:
mvn spring-boot:run
mvn verify                         # las pruebas usan H2 en memoria
```

## Variables de entorno

| Variable | Por defecto | Uso |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/precificamos?...` | URL JDBC de MySQL |
| `DB_USER` / `DB_PASSWORD` | `root` / `root` | Credenciales |
| `FRONTEND_URL` | `http://localhost:4200` | Orígenes permitidos por CORS (separados por coma, con comodines) |
| `PORT` | `8080` | Puerto HTTP (Render lo define solo) |

## Organización del código

```
com.precificamos
  config/      CORS
  model/       entidades JPA (Produto, CustoFixo, ConfiguracaoNegocio…)
  service/     cálculo de productos guardados y excepciones
  segmento/    precificación por categoría
    Segmento, CalculadoraBase, SegmentoService, SegmentoController
    estrategia/  una estrategia por categoría (patrón Strategy)
    dto/         entradas y resultados
  web/         controladores de la API original y manejo de errores
```

Los errores se devuelven como JSON: `{"erro": "mensaje"}` y, en validaciones (HTTP 400),
`{"erro": "...", "campos": {"datos.costoCompra": "debe ser mayor que 0"}}`. Las reglas de negocio
imposibles (por ejemplo, margen + tasas ≥ 100 %) devuelven HTTP 422.

Ver el `README.md` de la raíz para la lista de endpoints y el despliegue en Render.
