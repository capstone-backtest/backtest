package com.backtest.user.controller;

import com.backtest.user.dto.auth.LoginRequest;
import com.backtest.user.dto.auth.RefreshTokenRequest;
import com.backtest.user.dto.auth.SignupRequest;
import com.backtest.user.dto.auth.TokenResponse;
import com.backtest.user.dto.UpdateProfileRequest;
import com.backtest.user.dto.UserResponse;
import com.backtest.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 사용자 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(
            @Valid @RequestBody SignupRequest request,
            HttpServletRequest httpRequest) {
        
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);
        
        UserResponse user = userService.signup(request, userAgent, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "로그인", description = "사용자 인증을 수행하고 JWT 토큰을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "계정이 비활성화됨")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);
        
        TokenResponse tokenResponse = userService.login(request, userAgent, ipAddress);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = userService.refreshToken(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 종료하고 토큰을 무효화합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        userService.logout(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "사용자 정보 조회", description = "사용자 ID로 공개 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "사용자 목록 조회", description = "사용자 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String search) {
        List<UserResponse> users = userService.getUsers(page, size, search);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "프로필 수정", description = "사용자 프로필 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse user = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "프로필 이미지 업로드", description = "사용자 프로필 이미지를 업로드합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파일 형식")
    })
    @PostMapping("/me/profile-image")
    public ResponseEntity<UserResponse> uploadProfileImage(
            @Parameter(description = "프로필 이미지 파일") @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse user = userService.updateProfileImage(userDetails.getUsername(), file);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "계정 비활성화", description = "사용자 계정을 비활성화합니다.")
    @ApiResponse(responseCode = "200", description = "계정 비활성화 성공")
    @PostMapping("/me/deactivate")
    public ResponseEntity<Void> deactivateAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deactivateAccount(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * 클라이언트 IP 주소를 추출합니다.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}