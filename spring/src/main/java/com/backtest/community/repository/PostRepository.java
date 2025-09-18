package com.backtest.community.repository;

import com.backtest.community.entity.Post;
import com.backtest.community.entity.PostCategory;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p where p.deleted = false and (:category is null or p.category = :category)")
    Page<Post> findAllActive(PostCategory category, Pageable pageable);

    Optional<Post> findByIdAndDeletedFalse(Long id);
}
