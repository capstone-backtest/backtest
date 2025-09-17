package com.backtest.repository;

import com.backtest.entity.UserSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 소셜 계정 Repository
 */
@Repository
public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {

    /**
     * 소셜 ID와 제공자로 계정 찾기
     */
    Optional<UserSocialAccount> findBySocialIdAndProvider(String socialId, UserSocialAccount.Provider provider);

    /**
     * 사용자의 소셜 계정 목록 조회
     */
    @Query("SELECT s FROM UserSocialAccount s WHERE s.user.id = :userId")
    List<UserSocialAccount> findByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 특정 제공자 계정 조회
     */
    @Query("SELECT s FROM UserSocialAccount s WHERE s.user.id = :userId AND s.provider = :provider")
    Optional<UserSocialAccount> findByUserIdAndProvider(
        @Param("userId") Long userId, 
        @Param("provider") UserSocialAccount.Provider provider
    );

    /**
     * 소셜 계정 존재 여부 확인
     */
    boolean existsBySocialIdAndProvider(String socialId, UserSocialAccount.Provider provider);
}