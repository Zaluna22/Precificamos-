// Configuración de producción. En Vercel, la variable API_URL reemplaza apiUrl durante el build
// (ver scripts/configurar-entorno.mjs). Cambiá este valor si publicás en otro lugar.
export const environment = {
  production: true,
  apiUrl: 'https://precificamos-api.onrender.com',
  moneda: 'BRL',
  localeMoneda: 'pt-BR',
};
