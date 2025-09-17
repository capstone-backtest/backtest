package com.backtest.domain.user.service;

import com.backtest.domain.user.dto.auth.LoginRequest;
import com.backtest.domain.user.dto.auth.RefreshTokenRequest;
import com.backtest.domain.user.dto.auth.SignupRequest;
import com.backtest.domain.user.dto.auth.TokenResponse;
import com.backtest.domain.user.dto.UpdateProfileRequest;
import com.backtest.domain.user.dto.UserResponse;
import com.backtest.domain.user.entity.User;
import com.backtest.domain.user.entity.UserSession;
import com.backtest.domain.user.repository.UserRepository;
import com.backtest.domain.user.repository.UserSessionRepository;
import com.backtest.global.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 서비스
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserService(
            UserRepository userRepository,
            UserSessionRepository userSessionRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public UserResponse signup(SignupRequest request, String userAgent, String ipAddress) {
        // 이메일 중복 검사
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 사용자명 중복 검사
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }

        // 사용자 생성
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    public TokenResponse login(LoginRequest request, String userAgent, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("비활성화된 계정입니다.");
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 세션 저장
        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession();
        session.setUser(user);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setUserAgent(userAgent);
        session.setIpAddress(ipAddress);
        session.setAccessExpiresAt(now.plusSeconds(jwtTokenProvider.getAccessTokenValidityInSeconds()));
        session.setRefreshExpiresAt(now.plusSeconds(jwtTokenProvider.getRefreshTokenValidityInSeconds()));

        userSessionRepository.save(session);

        // 마지막 로그인 시간 업데이트
        user.setLastLoginAt(now);
        userRepository.save(user);

        return new TokenResponse(accessToken, refreshToken, "Bearer", 
                jwtTokenProvider.getAccessTokenValidityInSeconds());
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String email = jwtTokenProvider.getEmailFromToken(request.getRefreshToken());
        
        UserSession session = userSessionRepository.findByRefreshTokenAndIsRevoked(request.getRefreshToken(), false)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 리프레시 토큰입니다."));

        if (session.getRefreshExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("만료된 리프레시 토큰입니다.");
        }

        // 새 액세스 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        
        // 세션 업데이트
        session.setAccessToken(newAccessToken);
        session.setAccessExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getAccessTokenValidityInSeconds()));
        session.setLastUsedAt(LocalDateTime.now());

        userSessionRepository.save(session);

        return new TokenResponse(newAccessToken, request.getRefreshToken(), "Bearer",
                jwtTokenProvider.getAccessTokenValidityInSeconds());
    }

    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 모든 활성 세션 무효화
        userSessionRepository.findByUserAndIsRevoked(user, false)
                .forEach(session -> {
                    session.setIsRevoked(true);
                    session.setRevokedAt(LocalDateTime.now());
                });
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return convertToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findByIdAndIsDeleted(userId, false)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return convertToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(int page, int size, String search) {
        PageRequest pageRequest = PageRequest.of(page, size);
        
        if (search != null && !search.trim().isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCaseAndIsDeleted(search, false, pageRequest)
                    .stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findByIsDeleted(false, pageRequest)
                    .stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList());
        }
    }

    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (request.getUsername() != null) {
            if (userRepository.findByUsername(request.getUsername()).isPresent() &&
                !user.getUsername().equals(request.getUsername())) {
                throw new RuntimeException("이미 존재하는 사용자명입니다.");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getInvestmentType() != null) {
            user.setInvestmentType(request.getInvestmentType());
        }

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    public UserResponse updateProfileImage(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 파일 업로드 로직 (구현 필요)
        String profileImageUrl = uploadFile(file);
        user.setProfileImage(profileImageUrl);

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    public void deactivateAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setIsActive(false);
        userRepository.save(user);

        // 모든 세션 무효화
        logout(email);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setProfileImage(user.getProfileImage());
        response.setInvestmentType(user.getInvestmentType());
        response.setIsAdmin(user.getIsAdmin());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private String uploadFile(MultipartFile file) {
        // 파일 업로드 구현 (S3, 로컬 저장소 등)
        // 임시로 파일명만 반환
        return "/uploads/" + file.getOriginalFilename();
    }
}