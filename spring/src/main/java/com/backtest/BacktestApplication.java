package com.backtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 백테스트 플랫폼 Spring Boot 애플리케이션 메인 클래스
 * 
 * 담당 기능:
 * - 사용자 인증 및 관리 (JWT)
 * - 커뮤니티 게시판 (게시글, 댓글, 좋아요)
 * - 실시간 채팅 (WebSocket)
 * - 소셜 로그인 (Google OAuth2)
 * - 관리자 기능
 */
@SpringBootApplication
@EnableAsync
@EnableJpaAuditing
public class BacktestApplication {

    public static void main(String[] args) {
        SpringApplication.run(BacktestApplication.class, args);
    }
}