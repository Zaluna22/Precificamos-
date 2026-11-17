// Regenera la copia de las categorías que la app muestra al instante (src/app/core/segmentos-foto).
// Correr después de cambiar segmentos en el backend: npm run segmentos:foto
// Usa API_URL si está definida; si no, la API de producción. La primera llamada puede tardar
// hasta un minuto si el servidor estaba dormido.
import { writeFile } from 'node:fs/promises';

const api = (process.env.API_URL ?? 'https://precificamos-api.onrender.com').replace(/\/$/, '');
for (const idioma of ['es', 'pt-BR', 'en']) {
  const r = await fetch(`${api}/api/segmentos`, { headers: { 'Accept-Language': idioma } });
  if (!r.ok) throw new Error(`${idioma}: HTTP ${r.status}`);
  const datos = await r.json();
  await writeFile(new URL(`../src/app/core/segmentos-foto/${idioma}.json`, import.meta.url), JSON.stringify(datos, null, 2) + '\n');
  console.log(`${idioma}: ${datos.length} categorías`);
}
