import { Component, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Community, Post, UserSummary } from '../../core/models';
import { Avatar } from '../../shared/avatar';
import { PostCard } from '../../shared/post-card';

@Component({
  selector: 'app-community',
  imports: [RouterLink, PostCard, Avatar],
  template: `
    <div class="stack">
      @if (community(); as c) {
        <section class="card stack">
          <div class="row">
            <h1 style="flex:1">{{ c.name }}</h1>
            <button class="btn btn-sm" [class.btn-outline]="c.member" (click)="toggle(c)">{{ c.member ? 'Membre ✓' : 'Rejoindre' }}</button>
          </div>
          <p class="muted" style="margin:0">{{ c.description }}</p>
          <div class="members">
            @for (m of members(); track m.id) {
              <a [routerLink]="['/profil', m.id]" [title]="m.fullName + (m.institution ? ' — ' + m.institution : '')">
                <app-avatar [name]="m.fullName" [size]="32" />
              </a>
            }
            <span class="muted small">{{ c.memberCount }} membre(s)</span>
          </div>
          @if (c.member) { <a class="btn btn-sm" routerLink="/publier">＋ Publier dans la communauté</a> }
        </section>
        @for (p of posts(); track p.id) {
          <app-post-card [post]="p" (deleted)="onDeleted($event)" />
        } @empty { <div class="card empty">Pas encore de publication dans cette communauté.</div> }
      }
    </div>`,
  styles: `.members { display: flex; align-items: center; gap: 4px; flex-wrap: wrap; span { margin-left: 8px; } }
    a.btn { align-self: flex-start; }`
})
export class CommunityPage {
  private api = inject(ApiService);
  id = input.required<string>();
  community = signal<Community | null>(null);
  posts = signal<Post[]>([]);

  onDeleted(id: number) {
    this.posts.set(this.posts().filter(p => p.id !== id));
  }
  members = signal<UserSummary[]>([]);

  constructor() {
    effect(() => {
      const id = Number(this.id());
      this.api.community(id).subscribe(c => this.community.set(c));
      this.api.communityPosts(id).subscribe(p => this.posts.set(p.items));
      this.api.communityMembers(id).subscribe(m => this.members.set(m.items.slice(0, 20)));
    });
  }

  toggle(c: Community) {
    this.api.joinCommunity(c.id, !c.member).subscribe(() =>
      this.community.set({ ...c, member: !c.member, memberCount: c.memberCount + (c.member ? -1 : 1) }));
  }
}
