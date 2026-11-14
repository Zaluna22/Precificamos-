import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Solo deja pasar con sesión; si no, manda a entrar y recuerda adónde quería ir. */
export const requiereSesion: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  if (auth.logueado()) return true;
  return inject(Router).createUrlTree(['/entrar'], { queryParams: { volver: estado.url } });
};

/** Para entrar y registro: si ya hay sesión, no tiene sentido mostrarlas. */
export const soloSinSesion: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.logueado() ? inject(Router).createUrlTree(['/']) : true;
};
