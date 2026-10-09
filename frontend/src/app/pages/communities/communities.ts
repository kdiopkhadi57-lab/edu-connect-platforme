import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Community } from '../../core/models';

@Component({
  selector: 'app-communities',
  imports: [FormsModule, RouterLink],
  template: `
    <div class="stack">
      <div class="card stack">
        <h1>Communautés</h1>
        <p class="muted" style="margin:0">Tu es inscrit·e automatiquement aux communautés de ton domaine, de ton niveau,
          de ta série et de ton établissement. Tu peux aussi en rejoindre d'autres ou créer la tienne (groupe de révision, club…).</p>
        <form (ngSubmit)="search()"><input name="q" [(ngModel)]="q" (ngModelChange)="search()" placeholder="Rechercher : Master, Droit, UCAD, Série S1…" /></form>
      </div>
      @for (c of list(); track c.id) {
        <div class="card row item">
          <div style="flex:1;min-width:0">
            <a class="name" [routerLink]="['/communaute', c.id]">{{ c.name }}</a>
            <p class="muted small">{{ c.description }}</p>
            <span class="muted small">{{ c.memberCount }} membre(s)</span>
          </div>
          <button class="btn btn-sm" [class.btn-outline]="c.member" (click)="toggle(c)">{{ c.member ? 'Membre ✓' : 'Rejoindre' }}</button>
        </div>
      }
      <form class="card stack" (ngSubmit)="create()">
        <h2>Créer une communauté</h2>
        <input name="n" [(ngModel)]="newName" placeholder="Nom (ex. : Révisions Bac S2 2026)" maxlength="100" />
        <textarea name="d" [(ngModel)]="newDesc" placeholder="Description" maxlength="500" rows="2"></textarea>
        <button class="btn" [disabled]="!newName.trim()">Créer</button>
      </form>
    </div>`,
  styles: `.name { font-weight: 700; color: var(--text); font-size: 16px; } p { margin: 4px 0; } .item { align-items: flex-start; }`
})
export class Communities {
  private api = inject(ApiService);
  private router = inject(Router);
  list = signal<Community[]>([]);
  q = '';
  newName = '';
  newDesc = '';

  constructor() { this.search(); }

  search() { this.api.searchCommunities(this.q).subscribe(l => this.list.set(l)); }

  toggle(c: Community) {
    this.api.joinCommunity(c.id, !c.member).subscribe(() =>
      this.list.set(this.list().map(x => x.id === c.id
        ? { ...x, member: !c.member, memberCount: x.memberCount + (c.member ? -1 : 1) } : x)));
  }

  create() {
    this.api.createCommunity(this.newName.trim(), this.newDesc.trim())
      .subscribe(c => this.router.navigate(['/communaute', c.id]));
  }
}
