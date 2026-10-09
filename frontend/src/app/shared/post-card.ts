import { Component, computed, inject, input, linkedSignal, output, signal } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Attachment, Post } from '../core/models';
import { Avatar } from './avatar';
import { FileSizePipe } from './file-size.pipe';
import { TimeAgoPipe } from './time-ago.pipe';

const TYPE_ICONS: Record<string, string> = {
  DOCUMENT: '📄', VIDEO: '🎬', EXERCICE: '✏️', EPREUVE: '📝', QUESTION: '❓', ANNONCE: '📢'
};

@Component({
  selector: 'app-post-card',
  imports: [RouterLink, Avatar, TimeAgoPipe, FileSizePipe],
  templateUrl: './post-card.html',
  styleUrl: './post-card.scss'
})
export class PostCard {
  private api = inject(ApiService);
  private sanitizer = inject(DomSanitizer);

  post = input.required<Post>();
  /** En mode détail, le contenu n'est pas tronqué. */
  full = input(false);
  deleted = output<number>();

  p = linkedSignal(() => this.post());
  showSolution = signal(false);
  expanded = signal(false);

  icon = computed(() => TYPE_ICONS[this.p().type] ?? '📄');
  images = computed(() => this.p().attachments.filter(a => a.contentType?.startsWith('image/')));
  videos = computed(() => this.p().attachments.filter(a => a.contentType?.startsWith('video/')));
  files = computed(() => this.p().attachments.filter(a =>
    !a.contentType?.startsWith('image/') && !a.contentType?.startsWith('video/')));
  longContent = computed(() => (this.p().content?.length ?? 0) > 400);
  embedUrl = computed<SafeResourceUrl | null>(() => {
    const url = this.p().videoUrl;
    if (!url) return null;
    const yt = url.match(/(?:youtube\.com\/(?:watch\?v=|embed\/|shorts\/)|youtu\.be\/)([\w-]{11})/);
    if (yt) return this.sanitizer.bypassSecurityTrustResourceUrl(`https://www.youtube-nocookie.com/embed/${yt[1]}`);
    const vimeo = url.match(/vimeo\.com\/(\d+)/);
    if (vimeo) return this.sanitizer.bypassSecurityTrustResourceUrl(`https://player.vimeo.com/video/${vimeo[1]}`);
    return null;
  });

  fileIcon(a: Attachment): string {
    const ext = a.name.split('.').pop()?.toLowerCase() ?? '';
    if (ext === 'pdf') return '📕';
    if (['doc', 'docx', 'odt', 'txt', 'md'].includes(ext)) return '📘';
    if (['ppt', 'pptx', 'odp'].includes(ext)) return '📙';
    if (['xls', 'xlsx', 'ods', 'csv'].includes(ext)) return '📗';
    if (['zip', 'rar', '7z'].includes(ext)) return '🗜️';
    if (['mp3', 'm4a', 'ogg'].includes(ext)) return '🎧';
    return '📎';
  }

  toggleLike() {
    const p = this.p();
    const on = !p.likedByMe;
    this.p.set({ ...p, likedByMe: on, likeCount: p.likeCount + (on ? 1 : -1) });
    this.api.like(p.id, on).subscribe({ error: () => this.p.set(p) });
  }

  toggleBookmark() {
    const p = this.p();
    this.p.set({ ...p, bookmarkedByMe: !p.bookmarkedByMe });
    this.api.bookmark(p.id, !p.bookmarkedByMe).subscribe({ error: () => this.p.set(p) });
  }

  share() {
    const url = `${location.origin}/publication/${this.p().id}`;
    if (navigator.share) {
      navigator.share({ title: this.p().title, url }).catch(() => {});
    } else {
      navigator.clipboard?.writeText(url);
      alert('Lien copié dans le presse-papiers');
    }
  }

  remove() {
    if (!confirm('Supprimer définitivement cette publication ?')) return;
    this.api.deletePost(this.p().id).subscribe(() => this.deleted.emit(this.p().id));
  }
}
