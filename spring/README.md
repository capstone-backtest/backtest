# Backtest Platform - Spring Boot Backend

백테스트 플랫폼의 모던한 Spring Boot 백엔드 서비스입니다. 도메인 중심 설계(DDD)와 최신 Spring Boot 기술 스택을 사용하여 구축되었습니다.

## 🏗️ 프로젝트 구조

```
src/main/java/com/backtest/
├── BacktestApplication.java           # 메인 애플리케이션
├── domain/                            # 도메인별 패키지
│   ├── user/                         # 사용자 도메인
│   │   ├── controller/               # REST 컨트롤러
│   │   ├── service/                  # 비즈니스 로직
│   │   ├── repository/               # 데이터 접근
│   │   ├── entity/                   # JPA 엔티티
│   │   └── dto/                      # 데이터 전송 객체
│   ├── post/                         # 게시글 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   └── chat/                         # 채팅 도메인
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── dto/
└── global/                           # 전역 설정 및 유틸리티
    ├── config/                       # Spring 설정
    └── util/                         # 공통 유틸리티
```

## 🚀 기술 스택

- **Java**: 21 (LTS)
- **Spring Boot**: 3.3.3
- **Build Tool**: Gradle 8.10
- **Database**: MySQL 8.0
- **Security**: Spring Security + JWT
- **WebSocket**: STOMP 프로토콜
- **Documentation**: SpringDoc OpenAPI 3
- **Testing**: JUnit 5, Testcontainers
- **Code Quality**: Jacoco (테스트 커버리지)

## 📋 주요 기능

### 🔐 사용자 관리
- JWT 기반 인증 시스템
- 회원가입, 로그인, 로그아웃
- 프로필 관리 (이미지 업로드 포함)
- 소셜 로그인 (Google OAuth2)
- 토큰 리프레시 및 세션 관리

### 📝 커뮤니티
- 게시글 작성, 수정, 삭제
- 댓글 및 대댓글 시스템
- 좋아요 기능
- 카테고리별 분류
- 검색 및 페이징

### 💬 실시간 채팅
- WebSocket 기반 실시간 채팅
- 채팅방 생성 및 관리
- 참가자 권한 관리
- 읽음 표시 기능
- 메시지 타입 지원 (텍스트, 이미지, 파일)

## 🛠️ 개발 환경 설정

### 요구사항
- Java 21 이상
- MySQL 8.0 이상
- Docker (선택사항)

### 로컬 개발 실행

1. **리포지토리 클론**
   ```bash
   git clone <repository-url>
   cd backtest/spring
   ```

2. **환경 설정**
   ```bash
   # application.properties 설정
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   # DB 연결 정보 수정
   ```

3. **데이터베이스 설정**
   ```sql
   CREATE DATABASE backtest_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

4. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

5. **API 문서 확인**
   ```
   http://localhost:8080/swagger-ui.html
   ```

## 🧪 테스트

### 전체 테스트 실행
```bash
./gradlew test
```

### 테스트 커버리지 확인
```bash
./gradlew jacocoTestReport
# 결과: build/reports/jacoco/test/html/index.html
```

### 특정 테스트만 실행
```bash
./gradlew test --tests "*.UserServiceTest"
```

## 📦 빌드 및 배포

### 애플리케이션 빌드
```bash
./gradlew build
```

### Docker 이미지 빌드
```bash
docker build -t backtest-backend .
```

### JAR 실행
```bash
java -jar build/libs/backtest-backend-1.0.0.jar
```

## 🔧 설정

### 주요 설정 파일
- `application.properties`: 메인 설정
- `build.gradle`: 빌드 설정 및 의존성
- `gradle.properties`: Gradle 성능 설정

### 환경별 설정
```bash
# 개발 환경
java -jar app.jar --spring.profiles.active=dev

# 운영 환경  
java -jar app.jar --spring.profiles.active=prod
```

## 🌐 API 엔드포인트

### 사용자 관리
- `POST /api/users/signup`: 회원가입
- `POST /api/users/login`: 로그인
- `POST /api/users/refresh`: 토큰 갱신
- `GET /api/users/me`: 내 정보 조회
- `PUT /api/users/me`: 프로필 수정

### 게시글
- `GET /api/posts`: 게시글 목록
- `POST /api/posts`: 게시글 작성
- `GET /api/posts/{id}`: 게시글 상세
- `PUT /api/posts/{id}`: 게시글 수정
- `DELETE /api/posts/{id}`: 게시글 삭제

### 채팅
- `GET /api/chat/rooms`: 채팅방 목록
- `POST /api/chat/rooms`: 채팅방 생성
- `POST /api/chat/rooms/{id}/join`: 채팅방 입장
- WebSocket `/ws/chat`: 실시간 메시징

## 🛡️ 보안

### JWT 토큰
- Access Token: 1시간 유효
- Refresh Token: 7일 유효
- 자동 토큰 갱신 지원

### CORS 설정
```properties
cors.allowed-origins=http://localhost:3000,http://localhost:5174
```

### 비밀번호 암호화
- BCrypt 해시 알고리즘 사용
- Salt 자동 생성

## 📊 모니터링

### Health Check
```
GET /actuator/health
```

### 메트릭 확인
```
GET /actuator/metrics
```

## 🐛 문제해결

### 일반적인 문제들

1. **데이터베이스 연결 실패**
   - MySQL 서버 실행 상태 확인
   - 연결 정보 (host, port, username, password) 확인

2. **JWT 토큰 오류**
   - 토큰 만료 시간 확인
   - 시크릿 키 설정 확인

3. **WebSocket 연결 실패**
   - CORS 설정 확인
   - 방화벽 설정 확인

## 👥 기여

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이선스

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 연락처

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.