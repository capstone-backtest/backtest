package com.backtest.community.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.backtest.auth.dto.RegisterRequest;
import com.backtest.auth.service.AuthService;
import com.backtest.community.dto.CommentRequest;
import com.backtest.community.dto.CommentResponse;
import com.backtest.community.dto.PostRequest;
import com.backtest.community.dto.PostResponse;
import com.backtest.community.entity.PostCategory;
import com.backtest.community.entity.PostContentType;
import com.backtest.global.security.UserPrincipal;
import com.backtest.user.entity.InvestmentType;
import com.backtest.user.entity.User;
import com.backtest.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CommunityServiceTest {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        if (principal == null) {
            var register = new RegisterRequest("writer", "writer@example.com", "Password123", InvestmentType.BALANCED);
            authService.register(register, "JUnit", "127.0.0.1");
            User user = userRepository.findByEmail("writer@example.com").orElseThrow();
            principal = UserPrincipal.from(user);
        }
    }

    @Test
    void createPostAndCommentFlow() {
        PostRequest postRequest = new PostRequest("테스트 제목", "본문 내용", PostCategory.GENERAL, PostContentType.MARKDOWN);
        PostResponse created = communityService.createPost(principal, postRequest);
        assertThat(created.title()).isEqualTo("테스트 제목");
        assertThat(created.commentCount()).isZero();

        CommentResponse comment = communityService.addComment(principal, created.id(), new CommentRequest("첫 댓글", null));
        assertThat(comment.content()).isEqualTo("첫 댓글");

        long likeCount = communityService.toggleLike(principal, created.id());
        assertThat(likeCount).isEqualTo(1);

        communityService.deleteComment(principal, comment.id());
        communityService.deletePost(principal, created.id());
    }
}
