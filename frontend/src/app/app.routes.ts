import { Routes } from '@angular/router';
import { requiereSesion, soloSinSesion } from './core/auth.guard';

export const routes: Routes = [
  { path: '', title: 'Precificamos · ¿Cuánto cobrar?', loadComponent: () => import('./pages/inicio/inicio').then((m) => m.Inicio) },
  { path: 'calcular/:codigo', title: 'Calcular precio · Precificamos', loadComponent: () => import('./pages/calcular/calcular').then((m) => m.Calcular) },
  { path: 'costos-fijos', canActivate: [requiereSesion], title: 'Costos fijos · Precificamos', loadComponent: () => import('./pages/costos-fijos/costos-fijos').then((m) => m.CostosFijos) },
  { path: 'configuracion', canActivate: [requiereSesion], title: 'Configuración · Precificamos', loadComponent: () => import('./pages/configuracion/configuracion').then((m) => m.ConfiguracionPagina) },
  { path: 'entrar', canActivate: [soloSinSesion], title: 'Entrar · Precificamos', loadComponent: () => import('./pages/entrar/entrar').then((m) => m.Entrar) },
  { path: 'registro', canActivate: [soloSinSesion], title: 'Crear cuenta · Precificamos', loadComponent: () => import('./pages/registro/registro').then((m) => m.Registro) },
  { path: '**', redirectTo: '' },
];
