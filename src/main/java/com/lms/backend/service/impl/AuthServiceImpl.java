package com.lms.backend.service.impl;

import com.lms.backend.dto.response.AccessTokenResponse;
import com.lms.backend.dto.request.LoginRequest;
import com.lms.backend.dto.LoginResult;
import com.lms.backend.dto.response.UserResponse;
import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.RefreshTokenEntity;
import com.lms.backend.repository.AccountRepository;
import com.lms.backend.repository.RoleRepository;
import com.lms.backend.security.JwtUtil;
import com.lms.backend.service.IAuthService;
import com.lms.backend.service.IRefreshTokenService;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private static final Logger log = Logger.getLogger(AuthServiceImpl.class.getName());
    @Value("${jwt.expiration}")
    private long accessTokenTtlSeconds;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final IRefreshTokenService refreshTokenService;

    @Override
    public LoginResult login(LoginRequest request, String userAgent) throws ExecutionException, InterruptedException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        AccountEntity account = accountRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        String accessToken = jwtUtil.generateToken(userDetails);
        RefreshTokenEntity refreshTokenEntity = refreshTokenService.createRefreshToken(account, userAgent);
        log.info("User " + account.getEmail() + " logged in");

        return new LoginResult(
                new AccessTokenResponse(accessToken, accessTokenTtlSeconds),
                refreshTokenEntity.getToken()
        );
    }

    @Override
    public LoginResult refresh(String refreshTokenValue, String userAgent) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenService.rotateRefreshToken(refreshTokenValue, userAgent);
        AccountEntity account = refreshTokenEntity.getAccount();
        UserDetails userDetails = userDetailsService.loadUserByUsername(account.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails);

        log.info("Token refreshed for: " + account.getEmail());
        return new LoginResult(
                new AccessTokenResponse(accessToken, accessTokenTtlSeconds, account.getEmail(), account.getRoleId()),
                refreshTokenEntity.getToken()
        );
    }


    @Override
    public void logout(String username) {
        AccountEntity account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Account not found"));
        refreshTokenService.revokeAllTokens(account);
        log.info("User " + username + " logged out");
    }

    @SneakyThrows
    @Override
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof String principalName && !"anonymousUser".equals(principalName)) {
            email = principalName;
        } else {
            throw new IllegalStateException("Authentication required");
        }

        AccountEntity account = accountRepository.findByUsername(email)
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        UserResponse response = new UserResponse();
        response.setId(account.getId());
        response.setUsername(account.getEmail());
        response.setEmail(account.getEmail());
        response.setRole(String.valueOf(roleRepository.findById(account.getRoleId())));
        response.setCreatedAt(account.getCreateDate());
        return response;
    }
}
