package com.backtest.service;

import com.backtest.entity.User;
import com.backtest.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 커스텀 사용자 상세 서비스
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> 
                    new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email)
        );

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> 
                    new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + id)
        );

        return UserPrincipal.create(user);
    }

    /**
     * 사용자 주체 클래스
     */
    public static class UserPrincipal implements UserDetails {
        
        private final Long id;
        private final String email;
        private final String password;
        private final List<GrantedAuthority> authorities;
        private final boolean enabled;

        public UserPrincipal(Long id, String email, String password, List<GrantedAuthority> authorities, boolean enabled) {
            this.id = id;
            this.email = email;
            this.password = password;
            this.authorities = authorities;
            this.enabled = enabled;
        }

        public static UserPrincipal create(User user) {
            List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );

            return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities,
                user.getIsActive()
            );
        }

        public Long getId() {
            return id;
        }

        @Override
        public String getUsername() {
            return email;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public List<GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}