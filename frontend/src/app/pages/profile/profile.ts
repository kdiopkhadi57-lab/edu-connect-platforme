import { Component, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Post, User } from '../../core/models';
import { Avatar } from '../../shared/avatar';
import { PostCard } from '../../shared/post-card';

@Component({
  selector: 'app-profile',
  imports: [RouterLink, Avatar, PostCard],
  template: `
    <div class="stack">
      @if (user(); as u) {
        <section class="card head">
          <app-avatar [name]="u.fullName" [size]="88" />
          <div class="info">
            <h1>{{ u.fullName }}</h1>
            <p class="muted">{{ u.roleLabel }}@if (u.institution) { · {{ u.institution }} }</p>
            <div class="chips">
              @if (u.field) { <span class="chip">{{ u.field.icon }} {{ u.field.name }}</span> }
              @if (u.levelLabel) { <span class="chip">🎓 {{ u.levelLabel }}</span> }
              @if (u.serie) { <span class="chip">Série {{ u.serie }}</span> }
            </div>
            @if (u.bio) { <p>{{ u.bio }}</p> }
            <div class="stats">
              <span><strong>{{ u.postCount }}</strong> publications</span>
              <span><strong>{{ u.followerCount }}</strong> abonnés</span>
              <span><strong>{{ u.followingCount }}</strong> abonnements</span>
            </div>
          </div>
          @if (u.id === auth.user()?.id) {
            <a class="btn btn-outline btn-sm" routerLink="/profil/modifier">Modifier</a>
          } @else {
            <button class="btn btn-sm" [class.btn-outline]="u.followedByMe" (click)="toggleFollow(u)">
              {{ u.followedByMe ? 'Abonné ✓' : 'Suivre' }}
            </button>
          }
        </section>
        <h2>Publications</h2>
        @for (p of posts(); track p.id) {
          <app-post-card [post]="p" (deleted)="onDeleted($event)" />
        } @empty { <div class="card empty">Aucune publication.</div> }
      }
    </div>`,
  styles: `
    .head { display: flex; gap: 20px; align-items: flex-start; }
    .info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; p { margin: 0; } }
    .chips { display: flex; gap: 6px; flex-wrap: wrap; }
    .chip { font-size: 12px; padding: 3px 9px; border-radius: 6px; background: var(--surface-2); color: var(--muted); }
    .stats { display: flex; gap: 16px; flex-wrap: wrap; font-size: 14px; color: var(--muted); strong { color: var(--text); } }
    @media (max-width: 600px) { .head { flex-direction: column; } }`
})
export class Profile {
  private api = inject(ApiService);
  auth = inject(AuthService);

  id = input.required<string>();
  user = signal<User | null>(null);
  posts = signal<Post[]>([]);

  onDeleted(id: number) {
    this.posts.set(this.posts().filter(p => p.id !== id));
  }

  constructor() {
    effect(() => {
      const id = Number(this.id());
      this.api.user(id).subscribe(u => this.user.set(u));
      this.api.userPosts(id).subscribe(p => this.posts.set(p.items));
    });
  }

  toggleFollow(u: User) {
    const on = !u.followedByMe;
    this.api.follow(u.id, on).subscribe(() =>
      this.user.set({ ...u, followedByMe: on, followerCount: u.followerCount + (on ? 1 : -1) }));
  }
}
