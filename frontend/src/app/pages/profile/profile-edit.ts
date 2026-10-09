import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/errors';
import { Role } from '../../core/models';

@Component({
  selector: 'app-profile-edit',
  imports: [FormsModule],
  template: `
    <form class="card stack" (ngSubmit)="save()" #f="ngForm">
      <h1>Mon parcours</h1>
      <p class="muted small" style="margin:0">Quand tu changes de niveau, de domaine ou d'établissement,
        tes communautés automatiques sont mises à jour.</p>
      @if (error()) { <div class="alert">{{ error() }}</div> }
      @if (loaded()) {
        <label class="field">Nom complet <input name="fullName" [(ngModel)]="model.fullName" required maxlength="100" /></label>
        <div class="grid-2">
          <label class="field">Je suis
            <select name="role" [(ngModel)]="model.role">
              @for (r of ref()?.roles; track r.code) { <option [value]="r.code">{{ r.label }}</option> }
            </select>
          </label>
          <label class="field">Établissement <input name="institution" [(ngModel)]="model.institution" maxlength="150" /></label>
          <label class="field">Domaine
            <select name="fieldId" [(ngModel)]="model.fieldId" required>
              @for (fd of ref()?.fields; track fd.id) { <option [ngValue]="fd.id">{{ fd.icon }} {{ fd.name }}</option> }
            </select>
          </label>
          <label class="field">Niveau
            <select name="level" [(ngModel)]="model.level" (ngModelChange)="level.set($event)" required>
              @for (l of ref()?.levels; track l.code) { <option [value]="l.code">{{ l.label }} ({{ l.group }})</option> }
            </select>
          </label>
        </div>
        @if (isLycee()) {
          <label class="field">Série <input name="serie" [(ngModel)]="model.serie" list="s" />
            <datalist id="s">@for (s of ref()?.series; track s) { <option [value]="s"></option> }</datalist>
          </label>
        }
        <label class="field">Bio <textarea name="bio" [(ngModel)]="model.bio" maxlength="500" rows="3"></textarea></label>
        <button class="btn" [disabled]="f.invalid || saving()">Enregistrer</button>
      }
    </form>`
})
export class ProfileEdit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);

  ref = toSignal(this.api.reference$);
  loaded = signal(false);
  saving = signal(false);
  error = signal<string | null>(null);
  level = signal('');
  isLycee = computed(() => this.ref()?.levels.find(l => l.code === this.level())?.group === 'Lycée');

  model = { fullName: '', role: 'ETUDIANT' as Role, institution: '', fieldId: 0, level: '', serie: '', bio: '' };

  constructor() {
    this.api.me().subscribe(u => {
      this.model = {
        fullName: u.fullName, role: u.role, institution: u.institution ?? '', fieldId: u.field?.id ?? 0,
        level: u.level ?? '', serie: u.serie ?? '', bio: u.bio ?? ''
      };
      this.level.set(this.model.level);
      this.loaded.set(true);
    });
  }

  save() {
    this.saving.set(true);
    this.api.updateMe({
      ...this.model, institution: this.model.institution || null,
      serie: this.isLycee() ? this.model.serie || null : null, bio: this.model.bio || null
    }).subscribe({
      next: u => { this.auth.user.set(u); location.assign(`/profil/${u.id}`); },
      error: e => { this.error.set(errorMessage(e)); this.saving.set(false); }
    });
  }
}
