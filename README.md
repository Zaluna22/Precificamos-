# Precificamos

Calculadora de precios para emprendedores: a partir de tus costos, impuestos y tasas te dice
**cuánto cobrar**, **cuánto ganás** y **cuánto tenés que vender para no perder** (punto de equilibrio).
Sirve para cualquier negocio porque trabaja con **categorías amplias**, según cómo obtenés lo que vendés.

| Categoría (`codigo`) | Para quién es | Qué pide |
|---|---|---|
| `PRODUCCION_ALIMENTOS` | Gastronomía y alimentos/bebidas elaborados (confitería, viandas, pizzería, heladería…) | Receta/ficha técnica, ingredientes con cantidad y unidad, rendimiento en porciones, merma %, gas/luz por tiempo de preparación, CMV objetivo % |
| `FABRICACION` | Fabricación propia (ropa, artesanías, cosmética, muebles, bijouterie…) | Materias primas con unidad y consumo por pieza, insumos/avíos, minutos de mano de obra × costo por minuto, merma %, margen minorista y mayorista |
| `REVENTA` | Comercio minorista de productos comprados listos (tienda, e-commerce, catálogo…) | Costo de compra, flete, impuestos de entrada, markup, margen mínimo de liquidación, stock para rotación |
| `VENTA_MASIVA` | Supermercado, almacén, autoservicio, kiosco | Margen por categoría de góndola (configurable), pérdida por vencimiento/rotura %, redondeo a x,99, promociones "lleve N pague M" |
| `SERVICIOS` | Servicios profesionales o técnicos (peluquería, electricista, diseño, clases…) | Horas por servicio × valor hora, materiales, desplazamiento, margen |

Todas comparten la misma base: **costos fijos**, **impuestos** (Simples Nacional %, o MEI como
costo fijo), **tasas de tarjeta y marketplace**, **comisiones**, **punto de equilibrio** y **simulador**.

## Estructura del repositorio

```
backend/    API REST en Java 17 + Spring Boot 3 + MySQL (H2 en las pruebas)
frontend/   Aplicación web en Angular 22 (componentes standalone), mobile-first, en español
legacy/     Primera versión estática (HTML/CSS/JS), conservada como referencia
.github/    CI: pruebas del backend y compilación del frontend
```

## Ejecutar en local

```bash
# Base MySQL + API en http://localhost:8080
docker compose up --build

# Frontend en http://localhost:4200 (usa la API local)
cd frontend && npm ci && npm start
```

Pruebas del backend: `cd backend && mvn verify` (usa H2 en memoria, no hace falta MySQL).

## API

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/segmentos` | Categorías con negocios de ejemplo y los pasos/campos del formulario |
| GET | `/api/segmentos/{segmento}` | Una categoría |
| POST | `/api/segmentos/{segmento}/calcular` | Precio sugerido, costo, margen, punto de equilibrio, desglose y avisos |
| POST | `/api/segmentos/{segmento}/simular` | Ganancia, margen y punto de equilibrio a un precio elegido |
| POST | `/api/auth/registro` | Crea una cuenta (`email`, `nombre`, `password` de 8 caracteres o más) y devuelve `token` + `usuario` |
| POST | `/api/auth/login` | Inicia sesión (`email`, `password`) y devuelve `token` + `usuario` |
| GET | `/api/auth/yo` | Datos del usuario del token 🔒 |
| GET/POST/DELETE | `/api/custos-fixos` | Costos fijos mensuales del usuario 🔒 |
| GET/PUT | `/api/configuracao` | Valor hora, impuestos, tasas y unidades por mes del usuario 🔒 |
| GET/POST/PUT/DELETE | `/api/produtos`, `/api/produtos/{id}/preco`, `/api/produtos/{id}/simulacao` | API original de productos del usuario (se mantiene por compatibilidad) 🔒 |

🔒 = requiere sesión: enviá la cabecera `Authorization: Bearer <token>`; sin token (o con uno
vencido) la API responde **401** con `{"erro": "..."}`. Cada usuario ve y modifica solo sus datos.

### Cuentas y sesión (JWT)

```bash
# Crear cuenta
curl -X POST http://localhost:8080/api/auth/registro -H 'Content-Type: application/json' \
  -d '{"email": "ana@ejemplo.com", "nombre": "Ana", "password": "una-clave-segura"}'
# → 201 {"token": "eyJ...", "usuario": {"id": 1, "email": "ana@ejemplo.com", "nombre": "Ana", "fechaCreacion": "..."}}

# Iniciar sesión
curl -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email": "ana@ejemplo.com", "password": "una-clave-segura"}'

# Usar el token
curl http://localhost:8080/api/custos-fixos -H 'Authorization: Bearer eyJ...'
```

- Las contraseñas se guardan con hash BCrypt. El token es un JWT firmado con HS256 y dura 7 días
  (configurable con `JWT_HORAS_EXPIRACION`).
- Errores: email con formato inválido o contraseña corta → 400 (con detalle en `campos`);
  email ya registrado → 409; email o contraseña incorrectos → 401.
- **Calcular y simular siguen siendo públicos.** Sin token se calcula con lo que mandás en `comun`
  (sin costos fijos ni configuración guardada); con token se completan los datos que faltan con los
  costos fijos y la configuración de ese usuario.

Ejemplo de cálculo:

```bash
curl -X POST http://localhost:8080/api/segmentos/SERVICIOS/calcular \
  -H 'Content-Type: application/json' \
  -d '{
        "comun": {"costosFijosMensuales": 1000, "impuestoPct": 6, "tarjetaPct": 4, "ventasMensualesProducto": 100, "unidadesMensualesNegocio": 500},
        "datos": {"nombreServicio": "Coloración", "horasPorServicio": 1.5, "valorHora": 30,
                  "materiales": [{"nombre": "Tintura", "costo": 18}],
                  "kmDesplazamiento": 20, "costoPorKm": 1, "otrosGastosDesplazamiento": 5, "margenPct": 30}
      }'
# → precioSugerido 150.00, margenRealPct 30.00, puntoEquilibrioUnidades 22
```

Los campos de `comun` que no se envían se completan, si hay sesión, con `/api/configuracao` y con
la suma de `/api/custos-fixos` del usuario. Para `/simular` se agrega `"precio": 120` al mismo cuerpo.

### Cómo se agrega una categoría nueva

Cada categoría es una estrategia (patrón *Strategy*): una clase que implementa
`EstrategiaPrecificacion<T>` en `backend/src/main/java/com/precificamos/segmento/estrategia/`, con
su DTO de entrada en `segmento/dto/`. Se registra sola por ser un `@Component`; solo hay que sumar
el valor al enum `Segmento` con su nombre, descripción y ejemplos. El formulario del frontend se
dibuja a partir de los pasos que declara la estrategia, así que no hace falta tocar Angular.

## Despliegue

### Backend en Render (Docker) + MySQL

1. En Render: **New → Web Service**, conectá este repositorio y elegí **Docker**.
   - **Root Directory**: `backend` (Render usa `backend/Dockerfile`).
   - O bien usá **New → Blueprint** con el `render.yaml` de la raíz.
2. **MySQL**: Render no ofrece MySQL administrado. Usá un MySQL externo (Aiven, Railway, PlanetScale
   compatible, el de tu hosting, etc.) o un servicio privado de Render con la imagen `mysql:8` y un
   disco persistente (planes pagos).
3. Variables de entorno del servicio:

   | Variable | Ejemplo |
   |---|---|
   | `DB_URL` | `jdbc:mysql://HOST:PUERTO/precificamos?useSSL=true&requireSSL=true&serverTimezone=America/Sao_Paulo` |
   | `DB_USER` | usuario de la base |
   | `DB_PASSWORD` | contraseña de la base |
   | `FRONTEND_URL` | `https://tu-app.vercel.app` (varios separados por coma; admite comodines, ej. `https://tu-app-*.vercel.app` para las vistas previas) |
   | `JWT_SECRET` | secreto para firmar los tokens: **obligatorio en producción**, al menos 32 caracteres al azar (ej. `openssl rand -base64 48`). El `render.yaml` lo genera solo (`generateValue: true`). Si lo cambiás, todas las sesiones abiertas se cierran |
   | `JWT_HORAS_EXPIRACION` | opcional, duración del token en horas (por defecto `168` = 7 días) |

   `PORT` la define Render y la API la usa automáticamente. Las tablas se crean solas al arrancar
   (`spring.jpa.hibernate.ddl-auto=update`): al pasar a la versión con login se agregan la tabla
   `usuario`, la tabla `configuracao_usuario` y la columna `usuario_id` (nullable) en `custo_fixo`
   y `produto`. Las filas anteriores al login quedan sin dueño y no las ve ningún usuario (no se
   borran); la tabla vieja `configuracao_negocio` queda sin uso.
4. En el plan gratuito el servicio se duerme cuando no se usa: la primera solicitud puede tardar
   alrededor de un minuto. El frontend muestra un aviso si no logra conectar.

### Frontend en Vercel

| Opción en Vercel | Valor |
|---|---|
| Root Directory | `frontend` |
| Framework Preset | Angular |
| Install Command | `npm ci` |
| Build Command | `npm run build` |
| Output Directory | `dist/precificamos` |
| Variable de entorno | `API_URL` = URL pública del backend en Render, sin barra final (ej. `https://precificamos-api.onrender.com`) |

`frontend/vercel.json` ya trae estos valores y la regla de *rewrite* para que las rutas de Angular
(`/calcular/...`, `/costos-fijos`) funcionen al recargar la página.

## Hoja de ruta

- Guardar productos calculados por categoría e historial de precios
- Exportar a PDF
- Comparación con precios de la competencia

## Licencia

Proyecto de dominio público: se puede usar, modificar y distribuir libremente.
