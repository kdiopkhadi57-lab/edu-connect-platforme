import { HttpEventType } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/errors';
import { CreatePostPayload, PostType } from '../../core/models';
import { FileSizePipe } from '../../shared/file-size.pipe';

@Component({
  selector: 'app-post-new',
  imports: [FormsModule, FileSizePipe],
  templateUrl: './post-new.html',
  styleUrl: './post-new.scss'
})
export class PostNew {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  ref = toSignal(this.api.reference$);
  communities = toSignal(this.api.myCommunities(), { initialValue: [] });

  type = signal<PostType>((this.route.snapshot.queryParamMap.get('type') as PostType) ?? 'DOCUMENT');
  files = signal<File[]>([]);
  progress = signal<number | null>(null);
  error = signal<string | null>(null);
  dragOver = signal(false);

  model = {
    title: '', content: '', solution: '', videoUrl: '', tags: '',
    fieldId: this.auth.user()?.field?.id ?? null as number | null,
    level: this.auth.user()?.level ?? null as string | null,
    serie: this.auth.user()?.serie ?? '',
    communityId: null as number | null
  };

  typeCards: { type: PostType; icon: string; label: string; hint: string }[] = [
    { type: 'DOCUMENT', icon: '📄', label: 'Cours / Document', hint: 'PDF, Word, PowerPoint, fiches…' },
    { type: 'VIDEO', icon: '🎬', label: 'Vidéo', hint: 'Fichier vidéo ou lien YouTube' },
    { type: 'EXERCICE', icon: '✏️', label: 'Exercice', hint: 'Énoncé + corrigé masqué' },
    { type: 'EPREUVE', icon: '📝', label: "Sujet d'examen", hint: 'Annales, bac, partiels…' },
    { type: 'QUESTION', icon: '❓', label: 'Question', hint: "Demande de l'aide" },
    { type: 'ANNONCE', icon: '📢', label: 'Annonce', hint: 'Stage, événement, groupe de révision' }
  ];

  withSolution = computed(() => this.type() === 'EXERCICE' || this.type() === 'EPREUVE');
  accept = computed(() => this.type() === 'VIDEO' ? 'video/*' : '');

  onFiles(list: FileList | null) {
    if (!list) return;
    this.files.set([...this.files(), ...Array.from(list)].slice(0, 10));
  }

  onDrop(e: DragEvent) {
    e.preventDefault();
    this.dragOver.set(false);
    this.onFiles(e.dataTransfer?.files ?? null);
  }

  removeFile(i: number) {
    this.files.set(this.files().filter((_, idx) => idx !== i));
  }

  submit() {
    this.error.set(null);
    const payload: CreatePostPayload = {
      type: this.type(),
      title: this.model.title,
      content: this.model.content || null,
      solution: this.withSolution() ? this.model.solution || null : null,
      fieldId: this.model.fieldId,
      level: this.model.level,
      serie: this.model.serie || null,
      communityId: this.model.communityId,
      videoUrl: this.type() === 'VIDEO' ? this.model.videoUrl || null : null,
      tags: this.model.tags.split(/[,#]/).map(t => t.trim()).filter(Boolean)
    };
    this.progress.set(0);
    this.api.createPost(payload, this.files()).subscribe({
      next: ev => {
        if (ev.type === HttpEventType.UploadProgress && ev.total) {
          this.progress.set(Math.round((ev.loaded / ev.total) * 100));
        } else if (ev.type === HttpEventType.Response && ev.body) {
          this.router.navigate(['/publication', ev.body.id]);
        }
      },
      error: e => { this.error.set(errorMessage(e)); this.progress.set(null); }
    });
  }
}
