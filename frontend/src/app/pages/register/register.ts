import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/errors';
import { RegisterPayload, Role } from '../../core/models';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: '../auth.scss'
})
export class Register {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);

  ref = toSignal(this.api.reference$);
  step = signal(1);
  loading = signal(false);
  error = signal<string | null>(null);

  model = {
    fullName: '', email: '', password: '', role: 'ETUDIANT' as Role,
    institution: '', fieldId: null as number | null, level: '', serie: ''
  };
  // Signaux miroir pour l'aperçu des communautés
  fieldId = signal<number | null>(null);
  level = signal('');
  serie = signal('');
  institution = signal('');

  levelGroups = computed(() => {
    const groups = new Map<string, { code: string; label: string }[]>();
    for (const l of this.ref()?.levels ?? []) {
      groups.set(l.group!, [...(groups.get(l.group!) ?? []), l]);
    }
    return [...groups.entries()];
  });
  isLycee = computed(() => this.ref()?.levels.find(l => l.code === this.level())?.group === 'Lycée');

  previewCommunities = computed(() => {
    const ref = this.ref();
    const field = ref?.fields.find(f => f.id === this.fieldId());
    const level = ref?.levels.find(l => l.code === this.level());
    const out: string[] = [];
    if (field) out.push(`${field.name} — tous niveaux`);
    if (field && level) out.push(`${field.name} · ${level.label}`);
    if (this.isLycee() && this.serie() && level) out.push(`Série ${this.serie().toUpperCase()} · ${level.label}`);
    if (this.institution().trim()) out.push(this.institution().trim());
    return out;
  });

  next() { this.step.set(2); }

  submit() {
    this.loading.set(true);
    this.error.set(null);
    const payload: RegisterPayload = {
      ...this.model,
      fieldId: this.model.fieldId!,
      institution: this.model.institution || null,
      serie: this.isLycee() ? this.model.serie || null : null
    };
    this.auth.register(payload).subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: e => { this.error.set(errorMessage(e)); this.loading.set(false); }
    });
  }
}
