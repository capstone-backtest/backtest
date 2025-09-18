package com.backtest.user.service;

import com.backtest.global.exception.ApiException;
import com.backtest.user.dto.UserProfileUpdateRequest;
import com.backtest.user.dto.UserSummary;
import com.backtest.user.entity.User;
import com.backtest.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public UserSummary updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        user.updateProfile(request.profileImage(), request.investmentType());
        return toSummary(user);
    }

    public UserSummary toSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImage(),
                user.getInvestmentType(),
                user.isAdmin(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }

    public boolean matchesPassword(User user, String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, new String(user.getPasswordHash(), StandardCharsets.UTF_8));
    }

    @Transactional
    public void updatePassword(User user, String encodedPassword, byte[] salt) {
        user.setPassword(encodedPassword.getBytes(StandardCharsets.UTF_8), salt);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}
