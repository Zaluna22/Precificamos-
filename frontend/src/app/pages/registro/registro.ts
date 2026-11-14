import { Component, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, rutaSegura } from '../../core/auth.service';
import { TraducirPipe, t } from '../../core/i18n/i18n';

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** Alta de cuenta: nombre, email y contraseña (mínimo 8 caracteres). */
@Component({
  selector: 'app-registro',
  imports: [FormsModule, RouterLink, TraducirPipe],
  templateUrl: './registro.html',
})
export class Registro {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly volver = input<string | undefined>();

  protected nombre = '';
  protected email = '';
  protected password = '';
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly errores = signal<Record<string, string>>({});

  protected registrar(): void {
    const errores: Record<string, string> = {};
    const nombre = this.nombre.trim();
    const email = this.email.trim();
    if (!nombre) errores['nombre'] = t('Contanos cómo te llamás.');
    if (!EMAIL.test(email)) errores['email'] = t('Revisá el email: parece que no es válido.');
    if (this.password.length < 8) errores['password'] = t('La contraseña tiene que tener al menos 8 caracteres.');
    this.errores.set(errores);
    if (Object.keys(errores).length) {
      this.error.set(null);
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    this.auth.registro(nombre, email, this.password).subscribe({
      next: () => this.router.navigateByUrl(rutaSegura(this.volver())),
      error: (e) => {
        this.error.set(e.erro);
        if (e.campos) this.errores.set(e.campos);
        this.enviando.set(false);
      },
    });
  }
}
