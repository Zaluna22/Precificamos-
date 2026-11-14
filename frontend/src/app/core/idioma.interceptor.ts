import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { idioma } from './i18n/i18n';

/** Envía el idioma elegido a la API (Accept-Language) para recibir textos y errores traducidos. */
export const idiomaInterceptor: HttpInterceptorFn = (req, next) =>
  next(req.url.startsWith(environment.apiUrl) ? req.clone({ setHeaders: { 'Accept-Language': idioma() } }) : req);
