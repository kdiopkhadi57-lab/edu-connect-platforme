import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import {
  AuthResponse, Comment, Community, CreatePostPayload, ExploreFilters, Page, Post, ProfilePayload,
  Reference, RegisterPayload, User, UserSummary
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private base = '/api';

  readonly reference$: Observable<Reference> = this.http.get<Reference>(`${this.base}/reference`).pipe(shareReplay(1));

  // Auth
  login(email: string, password: string) {
    return this.http.post<AuthResponse>(`${this.base}/auth/login`, { email, password });
  }
  register(payload: RegisterPayload) {
    return this.http.post<AuthResponse>(`${this.base}/auth/register`, payload);
  }

  // Utilisateurs
  me() { return this.http.get<User>(`${this.base}/users/me`); }
  updateMe(payload: ProfilePayload & { bio: string | null }) { return this.http.put<User>(`${this.base}/users/me`, payload); }
  user(id: number) { return this.http.get<User>(`${this.base}/users/${id}`); }
  userPosts(id: number, page = 0) { return this.http.get<Page<Post>>(`${this.base}/users/${id}/posts`, { params: { page } }); }
  searchUsers(q: string) { return this.http.get<UserSummary[]>(`${this.base}/users`, { params: { q } }); }
  follow(id: number, on: boolean) {
    const url = `${this.base}/users/${id}/follow`;
    return on ? this.http.post<void>(url, null) : this.http.delete<void>(url);
  }
  bookmarks(page = 0) { return this.http.get<Page<Post>>(`${this.base}/users/me/bookmarks`, { params: { page } }); }

  // Publications
  feed(page = 0) { return this.http.get<Page<Post>>(`${this.base}/posts/feed`, { params: { page } }); }
  explore(filters: ExploreFilters, page = 0) {
    let params = new HttpParams().set('page', page);
    Object.entries(filters).forEach(([k, v]) => {
      if (v !== null && v !== undefined && v !== '') params = params.set(k, String(v));
    });
    return this.http.get<Page<Post>>(`${this.base}/posts`, { params });
  }
  post(id: number) { return this.http.get<Post>(`${this.base}/posts/${id}`); }
  createPost(data: CreatePostPayload, files: File[]) {
    const form = new FormData();
    form.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    files.forEach(f => form.append('files', f, f.name));
    return this.http.post<Post>(`${this.base}/posts`, form, { reportProgress: true, observe: 'events' });
  }
  deletePost(id: number) { return this.http.delete<void>(`${this.base}/posts/${id}`); }
  like(id: number, on: boolean) {
    const url = `${this.base}/posts/${id}/like`;
    return on ? this.http.post<void>(url, null) : this.http.delete<void>(url);
  }
  bookmark(id: number, on: boolean) {
    const url = `${this.base}/posts/${id}/bookmark`;
    return on ? this.http.post<void>(url, null) : this.http.delete<void>(url);
  }
  comments(postId: number) { return this.http.get<Page<Comment>>(`${this.base}/posts/${postId}/comments`); }
  addComment(postId: number, content: string) {
    return this.http.post<Comment>(`${this.base}/posts/${postId}/comments`, { content });
  }
  deleteComment(postId: number, commentId: number) {
    return this.http.delete<void>(`${this.base}/posts/${postId}/comments/${commentId}`);
  }

  // Communautés
  myCommunities() { return this.http.get<Community[]>(`${this.base}/communities/mine`); }
  searchCommunities(q: string) { return this.http.get<Community[]>(`${this.base}/communities`, { params: { q } }); }
  community(id: number) { return this.http.get<Community>(`${this.base}/communities/${id}`); }
  communityPosts(id: number, page = 0) {
    return this.http.get<Page<Post>>(`${this.base}/communities/${id}/posts`, { params: { page } });
  }
  communityMembers(id: number) { return this.http.get<Page<UserSummary>>(`${this.base}/communities/${id}/members`); }
  createCommunity(name: string, description: string) {
    return this.http.post<Community>(`${this.base}/communities`, { name, description });
  }
  joinCommunity(id: number, on: boolean) {
    const url = `${this.base}/communities/${id}/join`;
    return on ? this.http.post<void>(url, null) : this.http.delete<void>(url);
  }
}
