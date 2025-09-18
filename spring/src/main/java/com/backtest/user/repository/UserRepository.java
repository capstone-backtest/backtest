package com.backtest.user.repository;

import com.backtest.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 찾기
     */
    Optional<User> findByEmail(String email);

    /**
     * 사용자명으로 사용자 찾기
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 사용자명 존재 여부 확인
     */
    boolean existsByUsername(String username);

    /**
     * 활성화된 사용자 찾기
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveByEmail(@Param("email") String email);

    /**
     * 활성화된 사용자 목록 조회
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Page<User> findAllActive(Pageable pageable);

    /**
     * 사용자 검색 (이름 또는 사용자명으로)
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "u.isActive = true")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 역할별 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    Page<User> findByRole(@Param("role") User.Role role, Pageable pageable);
}