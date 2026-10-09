import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ExploreFilters, Page, Post } from '../../core/models';
import { Avatar } from '../../shared/avatar';
import { PostCard } from '../../shared/post-card';

@Component({
  selector: 'app-feed',
  imports: [FormsModule, RouterLink, PostCard, Avatar],
  templateUrl: './feed.html',
  styleUrl: './feed.scss'
})
export class Feed {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  auth = inject(AuthService);

  mode = input<'feed' | 'explore'>('feed');
  ref = toSignal(this.api.reference$);
  query = toSignal(this.route.queryParamMap);

  posts = signal<Post[]>([]);
  page = signal(0);
  last = signal(true);
  loading = signal(false);
  filters: ExploreFilters = {};

  quickTypes = [
    { type: 'DOCUMENT', label: '📄 Cours' }, { type: 'VIDEO', label: '🎬 Vidéo' },
    { type: 'EXERCICE', label: '✏️ Exercice' }, { type: 'QUESTION', label: '❓ Question' }
  ];
  cycles = [
    { code: 'LYCEE', label: 'Lycée' }, { code: 'FORMATION_PRO', label: 'Formation pro' }, { code: 'SUPERIEUR', label: 'Supérieur' }
  ];
  levelsForCycle = computed(() => {
    const cycleLabel = this.cycles.find(c => c.code === this.query()?.get('cycle'))?.label;
    return (this.ref()?.levels ?? []).filter(l => !cycleLabel || l.group === cycleLabel);
  });

  constructor() {
    effect(() => {
      const q = this.query();
      if (!q) return;
      this.filters = {
        q: q.get('q'), fieldId: q.get('fieldId') ? Number(q.get('fieldId')) : null, level: q.get('level'),
        cycle: q.get('cycle') as ExploreFilters['cycle'], serie: q.get('serie'),
        type: q.get('type') as ExploreFilters['type'], sort: (q.get('sort') as 'recent' | 'popular') ?? 'recent'
      };
      this.load(0);
    });
  }

  private source(page: number): Observable<Page<Post>> {
    return this.mode() === 'feed' ? this.api.feed(page) : this.api.explore(this.filters, page);
  }

  load(page: number) {
    this.loading.set(true);
    this.source(page).subscribe({
      next: res => {
        this.posts.set(page === 0 ? res.items : [...this.posts(), ...res.items]);
        this.page.set(res.page);
        this.last.set(res.last);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  applyFilters() {
    const params: Record<string, string | number | null> = {};
    for (const [k, v] of Object.entries(this.filters)) params[k] = v === '' || v === undefined ? null : (v as string | number | null);
    if (params['sort'] === 'recent') params['sort'] = null;
    this.router.navigate(['/explorer'], { queryParams: params });
  }

  setCycle(code: string | null) {
    this.filters.cycle = code as ExploreFilters['cycle'];
    this.filters.level = null;
    this.applyFilters();
  }

  resetFilters() {
    this.router.navigate(['/explorer']);
  }

  hasFilters = computed(() => (this.query()?.keys.length ?? 0) > 0);

  onDeleted(id: number) {
    this.posts.set(this.posts().filter(p => p.id !== id));
  }
}
