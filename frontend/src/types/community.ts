/**
 * 커뮤니티 도메인 타입 정의
 */

import { BaseEntity, PaginatedResponse, User } from './shared';

// === 게시글 ===

export interface Post extends BaseEntity {
  title: string;
  content: string;
  excerpt?: string;
  slug?: string;
  author: User;
  category: PostCategory;
  tags: string[];
  status: PostStatus;
  visibility: PostVisibility;
  featuredImage?: string;
  metadata: PostMetadata;
  stats: PostStats;
}

export interface PostCategory {
  id: string;
  name: string;
  slug: string;
  description?: string;
  color?: string;
  icon?: string;
  parentId?: string;
}

export type PostStatus = 'draft' | 'published' | 'archived' | 'deleted';
export type PostVisibility = 'public' | 'private' | 'unlisted';

export interface PostMetadata {
  wordCount: number;
  readingTime: number;
  seoTitle?: string;
  seoDescription?: string;
  canonicalUrl?: string;
  lastModified: string;
}

export interface PostStats {
  views: number;
  likes: number;
  dislikes: number;
  comments: number;
  shares: number;
  bookmarks: number;
}

// === 댓글 ===

export interface Comment extends BaseEntity {
  postId: string;
  parentId?: string; // For nested comments
  author: User;
  content: string;
  status: CommentStatus;
  metadata: CommentMetadata;
  replies?: Comment[];
}

export type CommentStatus = 'pending' | 'approved' | 'spam' | 'deleted';

export interface CommentMetadata {
  ipAddress?: string;
  userAgent?: string;
  isEdited: boolean;
  editedAt?: string;
  moderatedBy?: string;
  moderatedAt?: string;
}

// === 좋아요/북마크 ===

export interface Like extends BaseEntity {
  userId: string;
  targetId: string;
  targetType: 'post' | 'comment';
  type: 'like' | 'dislike';
}

export interface Bookmark extends BaseEntity {
  userId: string;
  postId: string;
  folder?: string;
  notes?: string;
}

// === 태그 ===

export interface Tag {
  id: string;
  name: string;
  slug: string;
  description?: string;
  color?: string;
  postCount: number;
  trending?: boolean;
}

// === 팔로우 ===

export interface Follow extends BaseEntity {
  followerId: string;
  followingId: string;
  follower: User;
  following: User;
}

// === 알림 ===

export interface Notification extends BaseEntity {
  userId: string;
  type: NotificationType;
  title: string;
  message: string;
  data?: Record<string, any>;
  read: boolean;
  readAt?: string;
}

export type NotificationType = 
  | 'new_follower'
  | 'new_comment'
  | 'comment_reply'
  | 'post_like'
  | 'post_mention'
  | 'system_update'
  | 'moderation_action';

// === 검색 ===

export interface SearchResult {
  type: 'post' | 'user' | 'tag';
  id: string;
  title: string;
  summary: string;
  url: string;
  score: number;
  highlighted?: Record<string, string[]>;
}

export interface SearchQuery {
  q: string;
  filters?: {
    type?: ('post' | 'user' | 'tag')[];
    category?: string[];
    tags?: string[];
    author?: string[];
    dateRange?: {
      from: string;
      to: string;
    };
  };
  sort?: {
    field: 'relevance' | 'date' | 'popularity' | 'title';
    direction: 'asc' | 'desc';
  };
  pagination: {
    page: number;
    size: number;
  };
}

// === 모더레이션 ===

export interface ModerationAction extends BaseEntity {
  targetId: string;
  targetType: 'post' | 'comment' | 'user';
  moderatorId: string;
  action: ModerationActionType;
  reason: string;
  details?: string;
  reversedAt?: string;
  reversedBy?: string;
}

export type ModerationActionType = 
  | 'approve'
  | 'reject'
  | 'hide'
  | 'delete'
  | 'ban'
  | 'warn'
  | 'feature';

// === 리포트 ===

export interface Report extends BaseEntity {
  reporterId: string;
  targetId: string;
  targetType: 'post' | 'comment' | 'user';
  reason: ReportReason;
  description?: string;
  status: ReportStatus;
  reviewedBy?: string;
  reviewedAt?: string;
  resolution?: string;
}

export type ReportReason = 
  | 'spam'
  | 'harassment'
  | 'hate_speech'
  | 'violence'
  | 'misinformation'
  | 'copyright'
  | 'adult_content'
  | 'other';

export type ReportStatus = 'pending' | 'reviewed' | 'resolved' | 'dismissed';

// === 커뮤니티 통계 ===

export interface CommunityStats {
  totalPosts: number;
  totalUsers: number;
  totalComments: number;
  activeUsers: {
    daily: number;
    weekly: number;
    monthly: number;
  };
  popularPosts: Post[];
  trendingTags: Tag[];
  recentActivity: Activity[];
}

export interface Activity extends BaseEntity {
  userId: string;
  type: ActivityType;
  targetId: string;
  targetType: string;
  metadata?: Record<string, any>;
}

export type ActivityType = 
  | 'post_created'
  | 'post_liked'
  | 'post_commented'
  | 'user_followed'
  | 'post_bookmarked'
  | 'tag_followed';

// === API 요청/응답 ===

export interface CreatePostRequest {
  title: string;
  content: string;
  categoryId: string;
  tags: string[];
  visibility?: PostVisibility;
  featuredImage?: File;
}

export interface UpdatePostRequest extends Partial<CreatePostRequest> {
  id: string;
}

export interface CreateCommentRequest {
  postId: string;
  parentId?: string;
  content: string;
}

export interface PostListQuery {
  category?: string;
  tag?: string;
  author?: string;
  status?: PostStatus;
  sort?: 'latest' | 'popular' | 'trending' | 'oldest';
  page?: number;
  size?: number;
}

export type PostListResponse = PaginatedResponse<Post>;
export type CommentListResponse = PaginatedResponse<Comment>;
export type UserListResponse = PaginatedResponse<User>;

// === 사용자 프로필 확장 ===

export interface CommunityProfile {
  bio?: string;
  website?: string;
  location?: string;
  socialLinks?: {
    twitter?: string;
    linkedin?: string;
    github?: string;
  };
  stats: {
    postsCount: number;
    commentsCount: number;
    likesReceived: number;
    followersCount: number;
    followingCount: number;
  };
  badges: Badge[];
  joinedAt: string;
}

export interface Badge {
  id: string;
  name: string;
  description: string;
  icon: string;
  color: string;
  criteria: string;
  earnedAt: string;
}

// === 피드 ===

export interface FeedItem {
  id: string;
  type: 'post' | 'comment' | 'like' | 'follow';
  user: User;
  target?: Post | Comment;
  timestamp: string;
  metadata?: Record<string, any>;
}

export interface Feed {
  items: FeedItem[];
  pagination: {
    hasMore: boolean;
    nextCursor?: string;
  };
}