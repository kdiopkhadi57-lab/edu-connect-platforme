import { Component, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { errorMessage } from '../../core/errors';
import { Comment, Post } from '../../core/models';
import { Avatar } from '../../shared/avatar';
import { PostCard } from '../../shared/post-card';
import { TimeAgoPipe } from '../../shared/time-ago.pipe';

@Component({
  selector: 'app-post-detail',
  imports: [FormsModule, RouterLink, PostCard, Avatar, TimeAgoPipe],
  template: `
    <div class="stack">
      @if (error()) { <div class="alert">{{ error() }}</div> }
      @if (post(); as p) {
        <app-post-card [post]="p" [full]="true" (deleted)="router.navigateByUrl('/')" />
        <section class="card stack">
          <h2>Commentaires ({{ comments().length }})</h2>
          <form class="row" (ngSubmit)="send()">
            <input name="c" [(ngModel)]="draft" placeholder="Pose une question, remercie, propose une autre méthode…" style="flex:1" />
            <button class="btn" [disabled]="!draft.trim()">Envoyer</button>
          </form>
          @for (c of comments(); track c.id) {
            <div class="comment">
              <a [routerLink]="['/profil', c.author.id]"><app-avatar [name]="c.author.fullName" [size]="34" /></a>
              <div class="bubble">
                <div class="row">
                  <a class="cname" [routerLink]="['/profil', c.author.id]">{{ c.author.fullName }}</a>
                  <span class="muted small">{{ c.author.institution }} · {{ c.createdAt | timeAgo }}</span>
                  @if (c.mine || p.mine) { <button class="del" (click)="remove(c)">Supprimer</button> }
                </div>
                <p>{{ c.content }}</p>
              </div>
            </div>
          } @empty { <p class="muted">Aucun commentaire pour l'instant.</p> }
        </section>
      }
    </div>`,
  styles: `
    .comment { display: flex; gap: 10px; align-items: flex-start; }
    .bubble { flex: 1; background: var(--surface-2); border-radius: 12px; padding: 8px 12px; min-width: 0;
      p { margin: 4px 0 0; white-space: pre-wrap; } .row { gap: 8px; } }
    .cname { font-weight: 600; color: var(--text); }
    .del { margin-left: auto; background: none; border: 0; color: var(--muted); cursor: pointer; font-size: 12px; &:hover { color: var(--danger); } }`
})
export class PostDetail {
  private api = inject(ApiService);
  router = inject(Router);

  id = input.required<string>();
  post = signal<Post | null>(null);
  comments = signal<Comment[]>([]);
  error = signal<string | null>(null);
  draft = '';

  constructor() {
    effect(() => {
      const id = Number(this.id());
      this.api.post(id).subscribe({ next: p => this.post.set(p), error: e => this.error.set(errorMessage(e)) });
      this.api.comments(id).subscribe(c => this.comments.set(c.items));
    });
  }

  send() {
    const p = this.post()!;
    this.api.addComment(p.id, this.draft.trim()).subscribe(c => {
      this.comments.set([...this.comments(), c]);
      this.post.set({ ...p, commentCount: p.commentCount + 1 });
      this.draft = '';
    });
  }

  remove(c: Comment) {
    this.api.deleteComment(this.post()!.id, c.id).subscribe(() => {
      this.comments.set(this.comments().filter(x => x.id !== c.id));
      const p = this.post()!;
      this.post.set({ ...p, commentCount: p.commentCount - 1 });
    });
  }
}
