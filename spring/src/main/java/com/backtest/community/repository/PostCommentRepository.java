package com.backtest.community.repository;

import com.backtest.community.entity.PostComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostIdAndDeletedFalseOrderByCreatedAtAsc(Long postId);
}
