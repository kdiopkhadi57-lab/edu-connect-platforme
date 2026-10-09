export type Role = 'ELEVE' | 'ETUDIANT' | 'APPRENANT_PRO' | 'ENSEIGNANT';
export type PostType = 'DOCUMENT' | 'VIDEO' | 'EXERCICE' | 'EPREUVE' | 'QUESTION' | 'ANNONCE';
export type Cycle = 'LYCEE' | 'FORMATION_PRO' | 'SUPERIEUR';
export type CommunityType = 'FIELD_LEVEL' | 'SERIE_LEVEL' | 'INSTITUTION' | 'CUSTOM';

export interface Field { id: number; name: string; icon: string; }
export interface LabeledValue { code: string; label: string; group: string | null; }

export interface Reference {
  fields: Field[];
  levels: LabeledValue[];
  series: string[];
  roles: LabeledValue[];
  postTypes: LabeledValue[];
}

export interface UserSummary {
  id: number; fullName: string; roleLabel: string; institution: string | null;
  levelLabel: string | null; fieldName: string | null;
}

export interface User {
  id: number; fullName: string; email: string | null; role: Role; roleLabel: string;
  institution: string | null; field: Field | null; level: string | null; levelLabel: string | null;
  serie: string | null; bio: string | null; createdAt: string;
  postCount: number; followerCount: number; followingCount: number; followedByMe: boolean;
}

export interface Attachment { id: number; name: string; contentType: string | null; size: number; url: string; }

export interface Post {
  id: number; type: PostType; typeLabel: string; title: string; content: string | null;
  hasSolution: boolean; solution: string | null;
  field: Field | null; level: string | null; levelLabel: string | null; serie: string | null;
  communityId: number | null; communityName: string | null;
  videoUrl: string | null; tags: string[]; attachments: Attachment[];
  author: UserSummary; createdAt: string;
  likeCount: number; commentCount: number; downloadCount: number;
  likedByMe: boolean; bookmarkedByMe: boolean; mine: boolean;
}

export interface Comment { id: number; content: string; author: UserSummary; createdAt: string; mine: boolean; }

export interface Community {
  id: number; name: string; description: string | null; type: CommunityType; memberCount: number; member: boolean;
}

export interface Page<T> { items: T[]; page: number; totalPages: number; totalItems: number; last: boolean; }

export interface AuthResponse { token: string; user: User; }

export interface ProfilePayload {
  fullName: string; role: Role; institution: string | null; fieldId: number; level: string; serie: string | null;
}

export interface RegisterPayload extends ProfilePayload { email: string; password: string; }

export interface CreatePostPayload {
  type: PostType; title: string; content: string | null; solution: string | null;
  fieldId: number | null; level: string | null; serie: string | null; communityId: number | null;
  videoUrl: string | null; tags: string[];
}

export interface ExploreFilters {
  fieldId?: number | null; level?: string | null; cycle?: Cycle | null; serie?: string | null;
  type?: PostType | null; q?: string | null; sort?: 'recent' | 'popular';
}
