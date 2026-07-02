package com.lms.backend.controller;

import com.lms.backend.dto.LoginResult;
import com.lms.backend.dto.request.LoginRequest;
import com.lms.backend.dto.response.AccessTokenResponse;
import com.lms.backend.dto.response.UserResponse;
import com.lms.backend.exception.InvalidTokenException;
import com.lms.backend.service.IAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final IAuthService authService;
    private final Environment environment;

    @Value("${jwt.expiration}")
    private long refreshExpiration;

    @SneakyThrows
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletResponse response
    ) {
        LoginResult result = authService.login(request, userAgent);
        setRefreshTokenCookie(response, result.refreshTokenValue());
        return ResponseEntity.ok(result.accessTokenResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshTokenValue = extractRefreshTokenCookie(request);
        LoginResult result = authService.refresh(refreshTokenValue, userAgent);
        setRefreshTokenCookie(response, result.refreshTokenValue());
        return ResponseEntity.ok(result.accessTokenResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authService.logout(userDetails.getUsername());
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        UserResponse user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String tokenValue) {
        boolean secure = Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase);
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, tokenValue)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofMillis(refreshExpiration))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }


    private void clearRefreshTokenCookie(HttpServletResponse response) {
        boolean secure = Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase);
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new InvalidTokenException("refresh token missing");
        }
        return Arrays.stream(cookies)
                .filter(cookie -> REFRESH_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new InvalidTokenException("refresh token missing"));
    }
}
