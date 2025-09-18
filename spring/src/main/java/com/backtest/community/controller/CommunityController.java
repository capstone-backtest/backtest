package com.backtest.community.controller;

import com.backtest.community.dto.CommentRequest;
import com.backtest.community.dto.CommentResponse;
import com.backtest.community.dto.PostRequest;
import com.backtest.community.dto.PostResponse;
import com.backtest.community.dto.PostSummary;
import com.backtest.community.entity.PostCategory;
import com.backtest.community.service.CommunityService;
import com.backtest.global.exception.ApiException;
import com.backtest.global.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public Page<PostSummary> listPosts(@RequestParam(value = "category", required = false) String category,
                                       @PageableDefault(size = 20) Pageable pageable) {
        PostCategory categoryFilter = category == null ? null : PostCategory.from(category);
        return communityService.listPosts(categoryFilter, pageable);
    }

    @GetMapping("/posts/{postId}")
    public PostResponse getPost(@PathVariable Long postId) {
        return communityService.getPost(postId, true);
    }

    @PostMapping("/posts")
    public PostResponse createPost(@Valid @RequestBody PostRequest request) {
        return communityService.createPost(currentUser(), request);
    }

    @PutMapping("/posts/{postId}")
    public PostResponse updatePost(@PathVariable Long postId, @Valid @RequestBody PostRequest request) {
        return communityService.updatePost(currentUser(), postId, request);
    }

    @DeleteMapping("/posts/{postId}")
    public void deletePost(@PathVariable Long postId) {
        communityService.deletePost(currentUser(), postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public CommentResponse addComment(@PathVariable Long postId, @Valid @RequestBody CommentRequest request) {
        return communityService.addComment(currentUser(), postId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        communityService.deleteComment(currentUser(), commentId);
    }

    @PostMapping("/posts/{postId}/like")
    public long toggleLike(@PathVariable Long postId) {
        return communityService.toggleLike(currentUser(), postId);
    }

    private UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다.");
        }
        return principal;
    }
}
