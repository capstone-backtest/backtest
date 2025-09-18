package com.backtest.community.service;

import com.backtest.community.dto.CommentRequest;
import com.backtest.community.dto.CommentResponse;
import com.backtest.community.dto.PostRequest;
import com.backtest.community.dto.PostResponse;
import com.backtest.community.dto.PostSummary;
import com.backtest.community.entity.Post;
import com.backtest.community.entity.PostCategory;
import com.backtest.community.entity.PostComment;
import com.backtest.community.entity.PostLike;
import com.backtest.community.repository.PostCommentRepository;
import com.backtest.community.repository.PostLikeRepository;
import com.backtest.community.repository.PostRepository;
import com.backtest.global.exception.ApiException;
import com.backtest.global.security.UserPrincipal;
import com.backtest.user.entity.User;
import com.backtest.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommunityService {

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final PostLikeRepository likeRepository;
    private final UserRepository userRepository;

    public CommunityService(PostRepository postRepository,
                            PostCommentRepository commentRepository,
                            PostLikeRepository likeRepository,
                            UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    public Page<PostSummary> listPosts(PostCategory category, Pageable pageable) {
        return postRepository.findAllActive(category, pageable)
                .map(post -> new PostSummary(
                        post.getId(),
                        post.getTitle(),
                        post.getAuthor().getUsername(),
                        post.getCategory(),
                        post.getViewCount(),
                        post.getLikeCount(),
                        post.getCommentCount(),
                        post.getCreatedAt()
                ));
    }

    @Transactional
    public PostResponse getPost(Long postId, boolean increaseViewCount) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."));
        if (increaseViewCount) {
            post.increaseViewCount();
        }
        List<PostComment> comments = commentRepository.findByPostIdAndDeletedFalseOrderByCreatedAtAsc(postId);
        return toResponse(post, comments);
    }

    @Transactional
    public PostResponse createPost(UserPrincipal principal, PostRequest request) {
        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        Post post = Post.create(author, request.title(), request.content(), request.category(), request.contentType());
        Post saved = postRepository.save(post);
        return toResponse(saved, List.of());
    }

    @Transactional
    public PostResponse updatePost(UserPrincipal principal, Long postId, PostRequest request) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."));
        validateAuthorOrAdmin(principal, post);
        post.update(request.title(), request.content(), request.category(), request.contentType());
        return toResponse(post, commentRepository.findByPostIdAndDeletedFalseOrderByCreatedAtAsc(postId));
    }

    @Transactional
    public void deletePost(UserPrincipal principal, Long postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."));
        validateAuthorOrAdmin(principal, post);
        post.softDelete();
    }

    @Transactional
    public CommentResponse addComment(UserPrincipal principal, Long postId, CommentRequest request) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."));
        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        PostComment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "PARENT_COMMENT_NOT_FOUND", "상위 댓글을 찾을 수 없습니다."));
        }
        PostComment comment = PostComment.create(post, author, request.content(), parent);
        PostComment saved = commentRepository.save(comment);
        post.increaseCommentCount();
        List<PostComment> comments = commentRepository.findByPostIdAndDeletedFalseOrderByCreatedAtAsc(postId);
        return toCommentResponse(saved, comments);
    }

    @Transactional
    public void deleteComment(UserPrincipal principal, Long commentId) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."));
        if (!comment.getAuthor().getId().equals(principal.getId()) && principal.getAuthorities().stream()
                .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "COMMENT_DELETE_FORBIDDEN", "댓글을 삭제할 권한이 없습니다.");
        }
        comment.softDelete();
        Post post = comment.getPost();
        post.decreaseCommentCount();
    }

    @Transactional
    public long toggleLike(UserPrincipal principal, Long postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        return likeRepository.findByPostIdAndUserId(postId, user.getId())
                .map(existing -> {
                    likeRepository.delete(existing);
                    long count = likeRepository.countByPostId(postId);
                    updateLikeCount(post, count);
                    return count;
                })
                .orElseGet(() -> {
                    likeRepository.save(PostLike.of(post, user));
                    long count = likeRepository.countByPostId(postId);
                    updateLikeCount(post, count);
                    return count;
                });
    }

    private void updateLikeCount(Post post, long count) {
        post.setLikeCount(count);
    }

    private void validateAuthorOrAdmin(UserPrincipal principal, Post post) {
        boolean isAuthor = post.getAuthor().getId().equals(principal.getId());
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAuthor && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "POST_FORBIDDEN", "게시글을 수정할 권한이 없습니다.");
        }
    }

    private PostResponse toResponse(Post post, List<PostComment> comments) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getContentType(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getAuthor().getUsername(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                toCommentTree(comments)
        );
    }

    private CommentResponse toCommentResponse(PostComment target, List<PostComment> allComments) {
        Map<Long, CommentResponse> responseMap = buildCommentTree(allComments);
        return responseMap.get(target.getId());
    }

    private List<CommentResponse> toCommentTree(List<PostComment> comments) {
        return new ArrayList<>(buildCommentTree(comments).values()).stream()
                .filter(comment -> comment.parentId() == null)
                .collect(Collectors.toList());
    }

    private Map<Long, CommentResponse> buildCommentTree(List<PostComment> comments) {
        Map<Long, CommentResponseBuilder> builders = new LinkedHashMap<>();
        for (PostComment comment : comments) {
            builders.put(comment.getId(), new CommentResponseBuilder(comment));
        }
        for (CommentResponseBuilder builder : builders.values()) {
            if (builder.parentId != null) {
                CommentResponseBuilder parentBuilder = builders.get(builder.parentId);
                if (parentBuilder != null) {
                    parentBuilder.children.add(builder);
                }
            }
        }
        return builders.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    }

    private static final class CommentResponseBuilder {
        private final Long id;
        private final Long parentId;
        private final String content;
        private final String author;
        private final long likeCount;
        private final OffsetDateTime createdAt;
        private final List<CommentResponseBuilder> children = new ArrayList<>();

        private CommentResponseBuilder(PostComment comment) {
            this.id = comment.getId();
            this.parentId = comment.getParent() != null ? comment.getParent().getId() : null;
            this.content = comment.getContent();
            this.author = comment.getAuthor().getUsername();
            this.likeCount = comment.getLikeCount();
            this.createdAt = comment.getCreatedAt();
        }

        private CommentResponse build() {
            return new CommentResponse(id, parentId, content, author, likeCount, createdAt,
                    children.stream().map(CommentResponseBuilder::build).toList());
        }
    }
}
