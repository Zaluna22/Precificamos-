import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

/**
 * Agrega {@code Authorization: Bearer <token>} a las llamadas a nuestra API y, si la API responde
 * 401 (token vencido o inválido), cierra la sesión y lleva a la pantalla de entrar.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const esApi = req.url.startsWith(environment.apiUrl);
  const token = auth.token();
  const pedido = esApi && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(pedido).pipe(
    catchError((e: unknown) => {
      const esLogin = req.url.includes('/api/auth/login') || req.url.includes('/api/auth/registro');
      if (esApi && !esLogin && e instanceof HttpErrorResponse && e.status === 401) {
        const habiaSesion = auth.logueado();
        auth.salir();
        if (!router.url.startsWith('/entrar')) {
          const volver = router.url.startsWith('/registro') ? '/' : router.url;
          router.navigate(['/entrar'], { queryParams: { volver, vencida: habiaSesion ? 1 : null } });
        }
      }
      return throwError(() => e);
    }),
  );
};
