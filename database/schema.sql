-- =================================================================
-- 백테스팅 플랫폼 통합 데이터베이스 스키마 (개선된 버전)
-- 대상 DBMS: MySQL 8.0+
-- 버전: 3.0 - SpringBoot + FastAPI 분리 아키텍처 대응
-- =================================================================

SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET FOREIGN_KEY_CHECKS=0;

-- =================================================================
-- 1. 회원 관리 (SpringBoot가 담당할 테이블들)
-- =================================================================

-- 회원 기본 정보
CREATE TABLE IF NOT EXISTS users (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  username        VARCHAR(50)     NOT NULL COMMENT '닉네임 (고유)',
  email           VARCHAR(255)    NOT NULL COMMENT '이메일 (고유)',
  password_hash   VARBINARY(255)  NOT NULL COMMENT '비밀번호 해시',
  password_salt   VARBINARY(128)  NOT NULL COMMENT '비밀번호 솔트',
  password_algo   VARCHAR(50)     NOT NULL DEFAULT 'bcrypt' COMMENT '해시 알고리즘',
  profile_image   VARCHAR(500)    DEFAULT NULL COMMENT '프로필 이미지 URL',
  investment_type ENUM('conservative', 'moderate', 'balanced', 'aggressive', 'speculative') DEFAULT 'balanced' COMMENT '투자 성향',
  is_admin        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '관리자 여부',
  is_active       TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '계정 활성화 상태',
  is_email_verified TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '이메일 인증 여부',
  last_login_at   TIMESTAMP       NULL COMMENT '최종 로그인 시각',
  is_deleted      TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '논리삭제',
  created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일(UTC)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_users_email (email),
  UNIQUE KEY uq_users_username (username),
  KEY idx_users_created (created_at DESC),
  KEY idx_users_investment (investment_type),
  KEY idx_users_last_login (last_login_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='회원 기본 정보';

-- 회원 세션 (Access Token + Refresh Token 지원)
CREATE TABLE IF NOT EXISTS user_sessions (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '회원ID',
  access_token  VARCHAR(1024)   NOT NULL COMMENT 'JWT Access Token',
  refresh_token VARCHAR(512)    NOT NULL COMMENT 'JWT Refresh Token',
  token_type    VARCHAR(20)     NOT NULL DEFAULT 'Bearer' COMMENT '토큰 타입',
  user_agent    VARCHAR(500)    DEFAULT NULL COMMENT '접속 User Agent',
  ip_address    VARCHAR(45)     DEFAULT NULL COMMENT '접속 IP (IPv6 지원)',
  device_info   JSON            DEFAULT NULL COMMENT '기기 정보 (JSON)',
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  access_expires_at TIMESTAMP   NOT NULL COMMENT 'Access Token 만료일(UTC)',
  refresh_expires_at TIMESTAMP  NOT NULL COMMENT 'Refresh Token 만료일(UTC)',
  last_used_at  TIMESTAMP       NULL COMMENT '마지막 사용 시각',
  is_revoked    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '토큰 폐기 여부',
  revoked_at    TIMESTAMP       NULL COMMENT '폐기 시각',
  PRIMARY KEY (id),
  UNIQUE KEY uq_sessions_access_token (access_token(255)),
  UNIQUE KEY uq_sessions_refresh_token (refresh_token),
  KEY idx_sessions_user (user_id),
  KEY idx_sessions_created (created_at DESC),
  KEY idx_sessions_access_expires (access_expires_at),
  KEY idx_sessions_refresh_expires (refresh_expires_at),
  CONSTRAINT fk_sessions_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='회원 세션 (JWT 토큰 관리)';

-- 소셜 로그인 연동
CREATE TABLE IF NOT EXISTS user_social_accounts (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '회원ID',
  provider      ENUM('google', 'kakao', 'naver', 'github') NOT NULL COMMENT '소셜 로그인 제공자',
  provider_id   VARCHAR(100)    NOT NULL COMMENT '소셜 로그인 고유ID',
  provider_email VARCHAR(255)   DEFAULT NULL COMMENT '소셜 계정 이메일',
  provider_data JSON            DEFAULT NULL COMMENT '소셜 계정 추가 정보',
  is_active     TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '연동 활성화 상태',
  linked_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '연동 일시',
  last_used_at  TIMESTAMP       NULL COMMENT '마지막 사용 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_social_provider (provider, provider_id),
  KEY idx_social_user (user_id),
  KEY idx_social_provider (provider),
  CONSTRAINT fk_social_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='소셜 로그인 계정 연동';

-- =================================================================
-- 2. 커뮤니티 & 게시판 (SpringBoot가 담당할 테이블들)
-- =================================================================

-- 게시글
CREATE TABLE IF NOT EXISTS posts (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '작성자',
  category      ENUM('general', 'strategy', 'question', 'news', 'backtest_share') DEFAULT 'general' COMMENT '게시글 카테고리',
  title         VARCHAR(200)    NOT NULL COMMENT '제목',
  content       MEDIUMTEXT      NOT NULL COMMENT '내용 (Markdown 지원)',
  content_type  ENUM('text', 'markdown') DEFAULT 'markdown' COMMENT '내용 형식',
  view_count    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '조회수',
  like_count    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '좋아요 수',
  comment_count INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '댓글 수',
  is_pinned     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '상단 고정',
  is_featured   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '추천 게시글',
  is_deleted    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '논리삭제',
  deleted_at    TIMESTAMP       NULL COMMENT '삭제 일시',
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일(UTC)',
  PRIMARY KEY (id),
  KEY idx_posts_user (user_id),
  KEY idx_posts_category (category),
  KEY idx_posts_created (created_at DESC),
  KEY idx_posts_like_count (like_count DESC),
  KEY idx_posts_view_count (view_count DESC),
  KEY idx_posts_pinned (is_pinned DESC, created_at DESC),
  KEY idx_posts_featured (is_featured DESC, created_at DESC),
  CONSTRAINT fk_posts_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='커뮤니티 게시글';

-- 게시글 댓글 (대댓글 지원)
CREATE TABLE IF NOT EXISTS post_comments (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  post_id       BIGINT UNSIGNED NOT NULL COMMENT '게시글ID',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '작성자',
  parent_id     BIGINT UNSIGNED NULL COMMENT '부모 댓글ID (대댓글인 경우)',
  content       TEXT            NOT NULL COMMENT '댓글 내용',
  like_count    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '좋아요 수',
  is_deleted    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '논리삭제',
  deleted_at    TIMESTAMP       NULL COMMENT '삭제 일시',
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일(UTC)',
  PRIMARY KEY (id),
  KEY idx_comments_post (post_id),
  KEY idx_comments_user (user_id),
  KEY idx_comments_parent (parent_id),
  KEY idx_comments_created (created_at DESC),
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id)
    REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id)
    REFERENCES post_comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='게시글 댓글';

-- 게시글 좋아요
CREATE TABLE IF NOT EXISTS post_likes (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  post_id     BIGINT UNSIGNED NOT NULL COMMENT '게시글ID',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '사용자ID',
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_post_likes (post_id, user_id),
  KEY idx_post_likes_user (user_id),
  CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id)
    REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='게시글 좋아요';

-- 댓글 좋아요
CREATE TABLE IF NOT EXISTS comment_likes (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  comment_id  BIGINT UNSIGNED NOT NULL COMMENT '댓글ID',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '사용자ID',
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_comment_likes (comment_id, user_id),
  KEY idx_comment_likes_user (user_id),
  CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id)
    REFERENCES post_comments(id) ON DELETE CASCADE,
  CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='댓글 좋아요';

-- =================================================================
-- 3. 실시간 채팅 (SpringBoot WebSocket이 담당할 테이블들)
-- =================================================================

-- 채팅방
CREATE TABLE IF NOT EXISTS chat_rooms (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  name          VARCHAR(100)    NOT NULL COMMENT '채팅방 이름',
  description   TEXT            DEFAULT NULL COMMENT '채팅방 설명',
  room_type     ENUM('public', 'private', 'direct') DEFAULT 'public' COMMENT '채팅방 타입',
  max_members   INT UNSIGNED    DEFAULT 100 COMMENT '최대 멤버 수',
  current_members INT UNSIGNED  DEFAULT 0 COMMENT '현재 멤버 수',
  created_by    BIGINT UNSIGNED NOT NULL COMMENT '생성자',
  is_active     TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '활성화 상태',
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일(UTC)',
  PRIMARY KEY (id),
  KEY idx_rooms_type (room_type),
  KEY idx_rooms_created_by (created_by),
  KEY idx_rooms_created (created_at DESC),
  CONSTRAINT fk_rooms_creator FOREIGN KEY (created_by)
    REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='채팅방';

-- 채팅방 멤버
CREATE TABLE IF NOT EXISTS chat_room_members (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  room_id     BIGINT UNSIGNED NOT NULL COMMENT '채팅방ID',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '사용자ID',
  role        ENUM('member', 'moderator', 'admin') DEFAULT 'member' COMMENT '멤버 역할',
  joined_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '입장 일시',
  last_read_at TIMESTAMP      NULL COMMENT '마지막 읽음 일시',
  is_active   TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '활성 상태',
  PRIMARY KEY (id),
  UNIQUE KEY uq_room_member (room_id, user_id),
  KEY idx_members_user (user_id),
  KEY idx_members_role (role),
  CONSTRAINT fk_members_room FOREIGN KEY (room_id)
    REFERENCES chat_rooms(id) ON DELETE CASCADE,
  CONSTRAINT fk_members_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='채팅방 멤버';

-- 채팅 메시지
CREATE TABLE IF NOT EXISTS chat_messages (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  room_id     BIGINT UNSIGNED NOT NULL COMMENT '채팅방ID',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '발신자ID',
  message_type ENUM('text', 'image', 'file', 'system') DEFAULT 'text' COMMENT '메시지 타입',
  content     TEXT            NOT NULL COMMENT '메시지 내용',
  file_url    VARCHAR(500)    DEFAULT NULL COMMENT '첨부파일 URL',
  file_name   VARCHAR(255)    DEFAULT NULL COMMENT '첨부파일명',
  file_size   INT UNSIGNED    DEFAULT NULL COMMENT '파일 크기(bytes)',
  reply_to_id BIGINT UNSIGNED NULL COMMENT '답장 대상 메시지ID',
  is_deleted  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '논리삭제',
  deleted_at  TIMESTAMP       NULL COMMENT '삭제 일시',
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일(UTC)',
  PRIMARY KEY (id),
  KEY idx_messages_room_created (room_id, created_at DESC),
  KEY idx_messages_user (user_id),
  KEY idx_messages_reply (reply_to_id),
  KEY idx_messages_type (message_type),
  CONSTRAINT fk_messages_room FOREIGN KEY (room_id)
    REFERENCES chat_rooms(id) ON DELETE CASCADE,
  CONSTRAINT fk_messages_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_messages_reply FOREIGN KEY (reply_to_id)
    REFERENCES chat_messages(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='채팅 메시지';

-- =================================================================
-- 4. 신고 & 관리 시스템 (SpringBoot가 담당할 테이블들)
-- =================================================================

-- 신고
CREATE TABLE IF NOT EXISTS reports (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  reporter_id BIGINT UNSIGNED NOT NULL COMMENT '신고자',
  target_type ENUM('post', 'comment', 'user', 'chat_message') NOT NULL COMMENT '신고 대상 타입',
  target_id   BIGINT UNSIGNED NOT NULL COMMENT '신고 대상 ID',
  reason_type ENUM('spam', 'harassment', 'inappropriate', 'copyright', 'other') NOT NULL COMMENT '신고 사유 타입',
  reason_detail VARCHAR(500)  DEFAULT NULL COMMENT '신고 사유 상세',
  status      ENUM('pending', 'processed', 'rejected') NOT NULL DEFAULT 'pending' COMMENT '처리 상태',
  admin_id    BIGINT UNSIGNED NULL COMMENT '처리한 관리자',
  admin_memo  TEXT            DEFAULT NULL COMMENT '관리자 메모',
  action_taken ENUM('none', 'warning', 'suspension', 'deletion', 'ban') DEFAULT 'none' COMMENT '취한 조치',
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  processed_at TIMESTAMP      DEFAULT NULL COMMENT '처리일(UTC)',
  PRIMARY KEY (id),
  KEY idx_reports_reporter (reporter_id),
  KEY idx_reports_target (target_type, target_id),
  KEY idx_reports_status (status),
  KEY idx_reports_created (created_at DESC),
  CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_reports_admin FOREIGN KEY (admin_id)
    REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='신고';

-- 공지사항
CREATE TABLE IF NOT EXISTS notices (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  admin_id    BIGINT UNSIGNED NOT NULL COMMENT '작성 관리자',
  category    ENUM('system', 'maintenance', 'feature', 'policy') DEFAULT 'system' COMMENT '공지 카테고리',
  title       VARCHAR(200)    NOT NULL COMMENT '제목',
  content     MEDIUMTEXT      NOT NULL COMMENT '내용',
  is_pinned   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '상단 고정',
  is_popup    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '팝업 표시',
  popup_start_date DATE        DEFAULT NULL COMMENT '팝업 시작일',
  popup_end_date DATE          DEFAULT NULL COMMENT '팝업 종료일',
  is_deleted  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '논리삭제',
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일(UTC)',
  PRIMARY KEY (id),
  KEY idx_notices_admin (admin_id),
  KEY idx_notices_category (category),
  KEY idx_notices_created (created_at DESC),
  KEY idx_notices_pinned (is_pinned DESC, created_at DESC),
  KEY idx_notices_popup (is_popup, popup_start_date, popup_end_date),
  CONSTRAINT fk_notices_admin FOREIGN KEY (admin_id)
    REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='공지사항';

-- =================================================================
-- 5. 백테스트 관련 (FastAPI와 SpringBoot 공유)
-- =================================================================

-- 백테스트 히스토리 (개선된 버전)
CREATE TABLE IF NOT EXISTS backtest_history (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  user_id         BIGINT UNSIGNED NOT NULL COMMENT '사용자ID',
  session_id      VARCHAR(100)    DEFAULT NULL COMMENT '세션ID (익명 사용자용)',
  title           VARCHAR(200)    DEFAULT NULL COMMENT '백테스트 제목',
  description     TEXT            DEFAULT NULL COMMENT '백테스트 설명',
  ticker          VARCHAR(20)     NOT NULL COMMENT '종목 심볼',
  strategy_name   VARCHAR(100)    NOT NULL COMMENT '전략명',
  start_date      DATE            NOT NULL COMMENT '시작일',
  end_date        DATE            NOT NULL COMMENT '종료일',
  initial_cash    DECIMAL(19, 4)  NOT NULL COMMENT '초기 투자금',
  final_value     DECIMAL(19, 4)  DEFAULT NULL COMMENT '최종 평가금액',
  total_return    DECIMAL(10, 4)  DEFAULT NULL COMMENT '총 수익률(%)',
  annual_return   DECIMAL(10, 4)  DEFAULT NULL COMMENT '연간 수익률(%)',
  sharpe_ratio    DECIMAL(10, 4)  DEFAULT NULL COMMENT '샤프 지수',
  max_drawdown    DECIMAL(10, 4)  DEFAULT NULL COMMENT '최대 낙폭(%)',
  volatility      DECIMAL(10, 4)  DEFAULT NULL COMMENT '변동성(%)',
  win_rate        DECIMAL(5, 2)   DEFAULT NULL COMMENT '승률(%)',
  profit_factor   DECIMAL(10, 4)  DEFAULT NULL COMMENT '이익 팩터',
  strategy_params JSON            DEFAULT NULL COMMENT '전략 파라미터',
  result_data     JSON            DEFAULT NULL COMMENT '백테스트 결과 데이터',
  chart_data      JSON            DEFAULT NULL COMMENT '차트 표시용 데이터',
  is_public       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '공개 여부',
  is_featured     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '추천 여부',
  view_count      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '조회수',
  like_count      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '좋아요 수',
  is_deleted      TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '논리삭제',
  created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일(UTC)',
  PRIMARY KEY (id),
  KEY idx_backtest_user (user_id),
  KEY idx_backtest_session (session_id),
  KEY idx_backtest_ticker (ticker),
  KEY idx_backtest_strategy (strategy_name),
  KEY idx_backtest_created (created_at DESC),
  KEY idx_backtest_return (total_return DESC),
  KEY idx_backtest_public (is_public, created_at DESC),
  KEY idx_backtest_featured (is_featured, created_at DESC),
  CONSTRAINT fk_backtest_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='백테스트 실행 히스토리';

-- 백테스트 좋아요 (공유된 백테스트에 대한)
CREATE TABLE IF NOT EXISTS backtest_likes (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  backtest_id   BIGINT UNSIGNED NOT NULL COMMENT '백테스트ID',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '사용자ID',
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_backtest_likes (backtest_id, user_id),
  KEY idx_backtest_likes_user (user_id),
  CONSTRAINT fk_backtest_likes_backtest FOREIGN KEY (backtest_id)
    REFERENCES backtest_history(id) ON DELETE CASCADE,
  CONSTRAINT fk_backtest_likes_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='백테스트 좋아요';

-- =================================================================
-- 6. 백테스트 결과 공유 시스템
-- =================================================================

-- 공유 링크 (URL 기반 백테스트 공유)
CREATE TABLE IF NOT EXISTS shared_backtests (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  share_key       VARCHAR(50)     NOT NULL COMMENT '공유 키 (URL용)',
  backtest_id     BIGINT UNSIGNED NOT NULL COMMENT '원본 백테스트ID',
  creator_id      BIGINT UNSIGNED NOT NULL COMMENT '공유 생성자',
  share_type      ENUM('public', 'link_only', 'password') DEFAULT 'link_only' COMMENT '공유 타입',
  password_hash   VARCHAR(255)    DEFAULT NULL COMMENT '비밀번호 (password 타입용)',
  title           VARCHAR(200)    NOT NULL COMMENT '공유 제목',
  description     TEXT            DEFAULT NULL COMMENT '공유 설명',
  expires_at      TIMESTAMP       NULL COMMENT '만료일',
  max_views       INT UNSIGNED    DEFAULT NULL COMMENT '최대 조회수',
  current_views   INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '현재 조회수',
  allow_download  TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '다운로드 허용',
  allow_clone     TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '복사 허용',
  is_active       TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '활성화 상태',
  created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일(UTC)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_share_key (share_key),
  KEY idx_shared_backtest (backtest_id),
  KEY idx_shared_creator (creator_id),
  KEY idx_shared_created (created_at DESC),
  KEY idx_shared_expires (expires_at),
  CONSTRAINT fk_shared_backtest FOREIGN KEY (backtest_id)
    REFERENCES backtest_history(id) ON DELETE CASCADE,
  CONSTRAINT fk_shared_creator FOREIGN KEY (creator_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='백테스트 공유 링크';

-- 공유 액세스 로그
CREATE TABLE IF NOT EXISTS shared_backtest_access_logs (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  shared_backtest_id BIGINT UNSIGNED NOT NULL COMMENT '공유 백테스트ID',
  visitor_id      BIGINT UNSIGNED NULL COMMENT '방문자 ID (로그인한 경우)',
  visitor_ip      VARCHAR(45)     NOT NULL COMMENT '방문자 IP',
  user_agent      VARCHAR(500)    DEFAULT NULL COMMENT 'User Agent',
  access_type     ENUM('view', 'download', 'clone') NOT NULL COMMENT '접근 타입',
  accessed_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '접근 일시',
  PRIMARY KEY (id),
  KEY idx_access_shared (shared_backtest_id),
  KEY idx_access_visitor (visitor_id),
  KEY idx_access_time (accessed_at DESC),
  CONSTRAINT fk_access_shared FOREIGN KEY (shared_backtest_id)
    REFERENCES shared_backtests(id) ON DELETE CASCADE,
  CONSTRAINT fk_access_visitor FOREIGN KEY (visitor_id)
    REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='공유 백테스트 접근 로그';

-- =================================================================
-- 7. 시스템 & 설정 테이블
-- =================================================================

-- 시스템 설정
CREATE TABLE IF NOT EXISTS system_settings (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  setting_key   VARCHAR(100)    NOT NULL COMMENT '설정 키',
  setting_value TEXT            DEFAULT NULL COMMENT '설정 값',
  value_type    ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string' COMMENT '값 타입',
  category      VARCHAR(50)     DEFAULT 'general' COMMENT '설정 카테고리',
  description   TEXT            DEFAULT NULL COMMENT '설정 설명',
  is_editable   TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '수정 가능 여부',
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일(UTC)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_setting_key (setting_key),
  KEY idx_settings_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='시스템 설정';

-- 사용자별 설정
CREATE TABLE IF NOT EXISTS user_settings (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '사용자ID',
  setting_key   VARCHAR(100)    NOT NULL COMMENT '설정 키',
  setting_value TEXT            DEFAULT NULL COMMENT '설정 값',
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(UTC)',
  updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일(UTC)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_user_setting (user_id, setting_key),
  KEY idx_user_settings_key (setting_key),
  CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='사용자별 설정';

-- =================================================================
-- 8. 기본 데이터 삽입
-- =================================================================

-- 시스템 설정 기본값
INSERT IGNORE INTO system_settings (setting_key, setting_value, value_type, category, description) VALUES
('max_backtest_duration_days', '3650', 'number', 'backtest', '백테스트 최대 기간 (일)'),
('default_commission', '0.002', 'number', 'backtest', '기본 수수료율'),
('max_file_upload_size', '10485760', 'number', 'general', '최대 파일 업로드 크기 (bytes)'),
('enable_social_login', 'true', 'boolean', 'auth', '소셜 로그인 활성화'),
('enable_chat', 'true', 'boolean', 'feature', '채팅 기능 활성화'),
('enable_community', 'true', 'boolean', 'feature', '커뮤니티 기능 활성화'),
('maintenance_mode', 'false', 'boolean', 'system', '점검 모드'),
('site_title', '라고할때살걸', 'string', 'general', '사이트 제목'),
('site_description', 'AI 백테스팅 플랫폼', 'string', 'general', '사이트 설명');

-- 기본 채팅방 생성
INSERT IGNORE INTO chat_rooms (id, name, description, room_type, created_by) VALUES
(1, '🎯 백테스팅 전략 토론', '백테스팅 전략과 관련된 토론을 나누는 공간입니다', 'public', 1),
(2, '📊 시장 분석 & 뉴스', '시장 분석과 뉴스를 공유하는 공간입니다', 'public', 1),
(3, '❓ Q&A 질문답변', '백테스팅과 투자에 관한 질문과 답변을 나누는 공간입니다', 'public', 1);

SET FOREIGN_KEY_CHECKS=1;

-- 스크립트 완료 메시지
SELECT '백테스팅 플랫폼 데이터베이스 스키마 생성이 완료되었습니다.' AS message;

-- 생성된 테이블 목록 확인
SELECT 
    TABLE_NAME as '생성된 테이블',
    TABLE_COMMENT as '설명'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE()
ORDER BY TABLE_NAME;