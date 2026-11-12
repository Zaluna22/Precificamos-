// Genera src/environments/environment.ts a partir de variables de entorno en el build.
// En Vercel se define API_URL (por ejemplo https://precificamos-api.onrender.com).
// Si API_URL no está definida, se deja el archivo tal como está en el repositorio.
import { writeFileSync } from 'node:fs';

const apiUrl = process.env.API_URL?.trim().replace(/\/+$/, '');
if (!apiUrl) {
  console.log('[entorno] API_URL no definida: se usa src/environments/environment.ts del repositorio.');
  process.exit(0);
}

const moneda = process.env.MONEDA?.trim() || 'BRL';
const localeMoneda = process.env.LOCALE_MONEDA?.trim() || 'pt-BR';
const contenido = `// Archivo generado por scripts/configurar-entorno.mjs durante el build.
export const environment = {
  production: true,
  apiUrl: '${apiUrl}',
  moneda: '${moneda}',
  localeMoneda: '${localeMoneda}',
};
`;
writeFileSync(new URL('../src/environments/environment.ts', import.meta.url), contenido);
console.log(`[entorno] API_URL = ${apiUrl}`);
