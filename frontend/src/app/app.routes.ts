import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth.guard';
import { Shell } from './layout/shell';

export const routes: Routes = [
  { path: 'connexion', canActivate: [guestGuard], loadComponent: () => import('./pages/login/login').then(m => m.Login) },
  { path: 'inscription', canActivate: [guestGuard], loadComponent: () => import('./pages/register/register').then(m => m.Register) },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', loadComponent: () => import('./pages/feed/feed').then(m => m.Feed), data: { mode: 'feed' } },
      { path: 'explorer', loadComponent: () => import('./pages/feed/feed').then(m => m.Feed), data: { mode: 'explore' } },
      { path: 'publier', loadComponent: () => import('./pages/post-new/post-new').then(m => m.PostNew) },
      { path: 'publication/:id', loadComponent: () => import('./pages/post-detail/post-detail').then(m => m.PostDetail) },
      { path: 'profil/modifier', loadComponent: () => import('./pages/profile/profile-edit').then(m => m.ProfileEdit) },
      { path: 'profil/:id', loadComponent: () => import('./pages/profile/profile').then(m => m.Profile) },
      { path: 'communautes', loadComponent: () => import('./pages/communities/communities').then(m => m.Communities) },
      { path: 'communaute/:id', loadComponent: () => import('./pages/community/community').then(m => m.CommunityPage) },
      { path: 'enregistres', loadComponent: () => import('./pages/bookmarks/bookmarks').then(m => m.Bookmarks) }
    ]
  },
  { path: '**', redirectTo: '' }
];
