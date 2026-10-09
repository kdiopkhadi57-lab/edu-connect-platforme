import { Component, inject, signal } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { Post } from '../../core/models';
import { PostCard } from '../../shared/post-card';

@Component({
  selector: 'app-bookmarks',
  imports: [PostCard],
  template: `
    <div class="stack">
      <h1>Enregistrés</h1>
      @for (p of posts(); track p.id) { <app-post-card [post]="p" /> }
      @empty { <div class="card empty">Enregistre des cours et exercices avec 🔖 pour les retrouver ici pendant tes révisions.</div> }
    </div>`
})
export class Bookmarks {
  posts = signal<Post[]>([]);
  constructor() { inject(ApiService).bookmarks().subscribe(p => this.posts.set(p.items)); }
}
