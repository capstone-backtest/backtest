/**
 * 인증 및 사용자 관리 도메인 타입 정의
 */

import { BaseEntity, UserPreferences } from './shared';

// === 사용자 ===

export interface User extends BaseEntity {
  username: string;
  email: string;
  emailVerified: boolean;
  password?: never; // 보안상 클라이언트에서는 노출하지 않음
  profile: UserProfile;
  roles: Role[];
  permissions: Permission[];
  preferences: UserPreferences;
  security: SecuritySettings;
  status: UserStatus;
  metadata: UserMetadata;
}

export interface UserProfile {
  firstName?: string;
  lastName?: string;
  displayName?: string;
  avatar?: string;
  bio?: string;
  location?: string;
  website?: string;
  phone?: string;
  dateOfBirth?: string;
  gender?: 'male' | 'female' | 'other' | 'prefer_not_to_say';
  timezone?: string;
  locale?: string;
}

export type UserStatus = 'active' | 'inactive' | 'suspended' | 'pending_verification' | 'banned';

export interface UserMetadata {
  lastLoginAt?: string;
  lastActiveAt?: string;
  loginCount: number;
  ipAddress?: string;
  userAgent?: string;
  registrationSource?: 'web' | 'mobile' | 'api';
  emailVerifiedAt?: string;
  phoneVerifiedAt?: string;
}

// === 보안 설정 ===

export interface SecuritySettings {
  twoFactorEnabled: boolean;
  twoFactorMethod?: 'sms' | 'email' | 'authenticator';
  passwordChangedAt: string;
  sessionTimeout: number;
  allowedIpRanges?: string[];
  loginNotifications: boolean;
  securityQuestions?: SecurityQuestion[];
}

export interface SecurityQuestion {
  question: string;
  answer: string; // 해시된 답변
}

// === 역할 및 권한 ===

export interface Role extends BaseEntity {
  name: string;
  displayName: string;
  description?: string;
  permissions: Permission[];
  isSystem: boolean;
  color?: string;
}

export interface Permission extends BaseEntity {
  name: string;
  displayName: string;
  description?: string;
  resource: string;
  action: string;
  isSystem: boolean;
}

export type PermissionAction = 'create' | 'read' | 'update' | 'delete' | 'execute' | 'manage';

// === 인증 토큰 ===

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  scope?: string;
}

export interface TokenPayload {
  sub: string; // User ID
  email: string;
  username: string;
  roles: string[];
  permissions: string[];
  iat: number;
  exp: number;
  jti: string; // JWT ID
}

// === 세션 ===

export interface Session extends BaseEntity {
  userId: string;
  token: string;
  ipAddress: string;
  userAgent: string;
  location?: {
    country: string;
    city: string;
    coordinates?: [number, number];
  };
  expiresAt: string;
  isActive: boolean;
  lastActivityAt: string;
}

// === 로그인 기록 ===

export interface LoginAttempt extends BaseEntity {
  email?: string;
  username?: string;
  ipAddress: string;
  userAgent: string;
  success: boolean;
  failureReason?: LoginFailureReason;
  location?: {
    country: string;
    city: string;
  };
}

export type LoginFailureReason = 
  | 'invalid_credentials'
  | 'user_not_found'
  | 'account_suspended'
  | 'too_many_attempts'
  | 'two_factor_required'
  | 'email_not_verified';

// === 비밀번호 재설정 ===

export interface PasswordResetToken extends BaseEntity {
  userId: string;
  token: string;
  expiresAt: string;
  used: boolean;
  usedAt?: string;
}

// === 이메일 인증 ===

export interface EmailVerificationToken extends BaseEntity {
  userId: string;
  email: string;
  token: string;
  expiresAt: string;
  verified: boolean;
  verifiedAt?: string;
}

// === OAuth 연동 ===

export interface OAuthProvider {
  name: string;
  displayName: string;
  clientId: string;
  authorizeUrl: string;
  tokenUrl: string;
  userInfoUrl: string;
  scope: string[];
  enabled: boolean;
}

export interface OAuthAccount extends BaseEntity {
  userId: string;
  provider: string;
  providerId: string;
  email?: string;
  username?: string;
  displayName?: string;
  avatar?: string;
  accessToken?: string;
  refreshToken?: string;
  expiresAt?: string;
  metadata?: Record<string, any>;
}

// === API 키 관리 ===

export interface ApiKey extends BaseEntity {
  userId: string;
  name: string;
  key: string;
  permissions: string[];
  lastUsedAt?: string;
  expiresAt?: string;
  isActive: boolean;
  rateLimit?: {
    requestsPerHour: number;
    requestsPerDay: number;
  };
}

// === 감사 로그 ===

export interface AuditLog extends BaseEntity {
  userId?: string;
  action: string;
  resource: string;
  resourceId?: string;
  details: Record<string, any>;
  ipAddress: string;
  userAgent: string;
  success: boolean;
  errorMessage?: string;
}

// === API 요청/응답 ===

export interface LoginRequest {
  email?: string;
  username?: string;
  password: string;
  rememberMe?: boolean;
  twoFactorCode?: string;
}

export interface LoginResponse {
  user: User;
  tokens: AuthTokens;
  session: Session;
  requiresTwoFactor?: boolean;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  firstName?: string;
  lastName?: string;
  acceptTerms: boolean;
  acceptPrivacy: boolean;
  marketingConsent?: boolean;
}

export interface RegisterResponse {
  user: User;
  tokens?: AuthTokens;
  requiresEmailVerification: boolean;
  message: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  tokens: AuthTokens;
}

export interface PasswordResetRequest {
  email: string;
}

export interface PasswordResetConfirmRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  displayName?: string;
  bio?: string;
  location?: string;
  website?: string;
  phone?: string;
  timezone?: string;
  locale?: string;
}

export interface EnableTwoFactorRequest {
  method: 'sms' | 'email' | 'authenticator';
  phoneNumber?: string;
  email?: string;
}

export interface VerifyTwoFactorRequest {
  code: string;
  method: 'sms' | 'email' | 'authenticator';
}

export interface OAuthLoginRequest {
  provider: string;
  code: string;
  redirectUri: string;
}

// === 폼 상태 ===

export interface LoginFormState {
  email: string;
  password: string;
  rememberMe: boolean;
  twoFactorCode?: string;
  isLoading: boolean;
  errors: Record<string, string>;
}

export interface RegisterFormState {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  acceptTerms: boolean;
  acceptPrivacy: boolean;
  marketingConsent: boolean;
  isLoading: boolean;
  errors: Record<string, string>;
}

export interface ProfileFormState extends UserProfile {
  isLoading: boolean;
  isDirty: boolean;
  errors: Record<string, string>;
}

// === 인증 컨텍스트 ===

export interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<LoginResponse>;
  logout: () => Promise<void>;
  register: (data: RegisterRequest) => Promise<RegisterResponse>;
  refreshToken: () => Promise<void>;
  updateProfile: (data: UpdateProfileRequest) => Promise<User>;
  changePassword: (data: ChangePasswordRequest) => Promise<void>;
  requestPasswordReset: (email: string) => Promise<void>;
  confirmPasswordReset: (data: PasswordResetConfirmRequest) => Promise<void>;
  verifyEmail: (token: string) => Promise<void>;
  resendVerificationEmail: () => Promise<void>;
}

// === 권한 확인 ===

export interface PermissionCheck {
  resource: string;
  action: PermissionAction;
  resourceId?: string;
}

export interface RoleCheck {
  roles: string[];
  requireAll?: boolean;
}