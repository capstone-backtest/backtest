package com.backtest.user.controller;

import com.backtest.global.exception.ApiException;
import com.backtest.global.security.UserPrincipal;
import com.backtest.user.dto.UserProfileUpdateRequest;
import com.backtest.user.dto.UserSummary;
import com.backtest.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserSummary getMe() {
        UserPrincipal principal = currentUser();
        return userService.toSummary(userService.getById(principal.getId()));
    }

    @PutMapping("/me")
    public UserSummary updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        UserPrincipal principal = currentUser();
        return userService.updateProfile(principal.getId(), request);
    }

    private UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다.");
        }
        return principal;
    }
}
