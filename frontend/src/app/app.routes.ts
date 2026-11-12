import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', title: 'Precificamos · ¿Cuánto cobrar?', loadComponent: () => import('./pages/inicio/inicio').then((m) => m.Inicio) },
  { path: 'calcular/:codigo', title: 'Calcular precio · Precificamos', loadComponent: () => import('./pages/calcular/calcular').then((m) => m.Calcular) },
  { path: 'costos-fijos', title: 'Costos fijos · Precificamos', loadComponent: () => import('./pages/costos-fijos/costos-fijos').then((m) => m.CostosFijos) },
  { path: 'configuracion', title: 'Configuración · Precificamos', loadComponent: () => import('./pages/configuracion/configuracion').then((m) => m.ConfiguracionPagina) },
  { path: '**', redirectTo: '' },
];
