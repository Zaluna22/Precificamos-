import { Component, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, rutaSegura } from '../../core/auth.service';

/** Inicio de sesión con email y contraseña. */
@Component({
  selector: 'app-entrar',
  imports: [FormsModule, RouterLink],
  templateUrl: './entrar.html',
})
export class Entrar {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Adónde volver después de entrar (?volver=/costos-fijos). */
  readonly volver = input<string | undefined>();
  /** ?vencida=1 cuando la sesión anterior expiró. */
  readonly vencida = input<string | undefined>();

  protected email = '';
  protected password = '';
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected entrar(): void {
    const email = this.email.trim();
    if (!email || !this.password) {
      this.error.set('Completá tu email y tu contraseña.');
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    this.auth.login(email, this.password).subscribe({
      next: () => this.router.navigateByUrl(rutaSegura(this.volver())),
      error: (e) => { this.error.set(e.erro); this.enviando.set(false); },
    });
  }
}
