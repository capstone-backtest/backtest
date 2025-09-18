package com.backtest.post.controller;

import com.backtest.post.dto.CreatePostRequest;
import com.backtest.post.dto.UpdatePostRequest;
import com.backtest.post.dto.PostResponse;
import com.backtest.post.dto.PostSummaryResponse;
import com.backtest.post.entity.Post;
import com.backtest.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "게시글 관리 API")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "게시글 작성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PostResponse post = postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공")
    @GetMapping
    public ResponseEntity<Page<PostSummaryResponse>> getPosts(
            @Parameter(description = "카테고리 필터") @RequestParam(required = false) Post.Category category,
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String search,
            @Parameter(description = "인기 게시글만 조회") @RequestParam(defaultValue = "false") boolean featured,
            Pageable pageable) {
        Page<PostSummaryResponse> posts = postService.getPosts(category, search, featured, pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId) {
        PostResponse post = postService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PostResponse post = postService.updatePost(postId, request, userDetails.getUsername());
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 추가/제거합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "좋아요 처리 성공"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> toggleLike(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 게시글 조회", description = "현재 사용자가 작성한 게시글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내 게시글 조회 성공")
    @GetMapping("/my")
    public ResponseEntity<Page<PostSummaryResponse>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<PostSummaryResponse> posts = postService.getUserPosts(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "사용자 게시글 조회", description = "특정 사용자가 작성한 게시글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 게시글 조회 성공")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostSummaryResponse>> getUserPosts(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            Pageable pageable) {
        Page<PostSummaryResponse> posts = postService.getUserPostsByUserId(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "인기 게시글 조회", description = "조회수나 좋아요가 많은 인기 게시글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "인기 게시글 조회 성공")
    @GetMapping("/trending")
    public ResponseEntity<Page<PostSummaryResponse>> getTrendingPosts(
            @Parameter(description = "기간 (days)") @RequestParam(defaultValue = "7") int days,
            Pageable pageable) {
        Page<PostSummaryResponse> posts = postService.getTrendingPosts(days, pageable);
        return ResponseEntity.ok(posts);
    }
}