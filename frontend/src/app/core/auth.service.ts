import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { ApiService } from './api.service';
import { AuthResponse, RegisterPayload, User } from './models';

const TOKEN_KEY = 'educonnect.token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(ApiService);
  private router = inject(Router);

  readonly user = signal<User | null>(null);
  readonly isLoggedIn = computed(() => this.token() !== null);
  private readonly token = signal<string | null>(readToken());

  getToken() { return this.token(); }

  login(email: string, password: string) {
    return this.api.login(email, password).pipe(tap(res => this.store(res)));
  }

  register(payload: RegisterPayload) {
    return this.api.register(payload).pipe(tap(res => this.store(res)));
  }

  /** Recharge le profil au démarrage si un jeton est présent. */
  refresh() {
    if (this.token()) {
      this.api.me().subscribe({ next: u => this.user.set(u), error: () => this.logout() });
    }
  }

  logout() {
    try { localStorage.removeItem(TOKEN_KEY); } catch { /* stockage indisponible */ }
    this.token.set(null);
    this.user.set(null);
    this.router.navigateByUrl('/connexion');
  }

  private store(res: AuthResponse) {
    try { localStorage.setItem(TOKEN_KEY, res.token); } catch { /* stockage indisponible */ }
    this.token.set(res.token);
    this.user.set(res.user);
  }
}

function readToken(): string | null {
  try { return localStorage.getItem(TOKEN_KEY); } catch { return null; }
}
