import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Community } from '../core/models';
import { Avatar } from '../shared/avatar';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, FormsModule, Avatar],
  templateUrl: './shell.html',
  styleUrl: './shell.scss'
})
export class Shell {
  auth = inject(AuthService);
  private api = inject(ApiService);
  private router = inject(Router);

  communities = signal<Community[]>([]);
  search = '';
  menuOpen = signal(false);

  constructor() {
    this.auth.refresh();
    this.loadCommunities();
  }

  loadCommunities() {
    this.api.myCommunities().subscribe(c => this.communities.set(c));
  }

  doSearch() {
    this.router.navigate(['/explorer'], { queryParams: { q: this.search || null } });
  }

  communityIcon(c: Community) {
    return { FIELD_LEVEL: '📚', SERIE_LEVEL: '🎒', INSTITUTION: '🏫', CUSTOM: '👥' }[c.type];
  }
}
