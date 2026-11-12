# Precificamos · Frontend (Angular)

Interfaz web en español, pensada primero para el celular, que consume la API de `backend/`.

- **Inicio**: elegís la categoría de tu negocio (con ejemplos y buscador).
- **Calcular**: formulario paso a paso según la categoría, tarjeta de resultado y simulador de precio.
- **Costos fijos** y **Configuración**: datos que se usan como base en todos los cálculos.

## Desarrollo

```bash
npm ci
npm start            # http://localhost:4200, usa la API en http://localhost:8080
npm run build        # genera dist/precificamos
```

## URL de la API

- Desarrollo: `src/environments/environment.development.ts`.
- Producción: `src/environments/environment.ts`. Si al compilar existe la variable de entorno
  `API_URL`, el script `scripts/configurar-entorno.mjs` reescribe ese archivo con ese valor
  (así se configura en Vercel sin tocar el código). Opcionales: `MONEDA` (por defecto `BRL`) y
  `LOCALE_MONEDA` (por defecto `pt-BR`).

## Estructura

```
src/app/
  core/          modelos (tipos de la API), ApiService, formato de moneda y borradores locales
  componentes/   campo dinámico del formulario, tarjeta de resultado y simulador
  pages/         inicio, calcular, costos-fijos y configuracion
```

Todos los componentes son *standalone* y usan *signals*.
