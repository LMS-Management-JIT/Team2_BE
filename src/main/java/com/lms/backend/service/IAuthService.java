package com.lms.backend.service;

import com.lms.backend.dto.LoginResult;
import com.lms.backend.dto.request.LoginRequest;
import com.lms.backend.dto.response.UserResponse;

import java.util.concurrent.ExecutionException;

public interface IAuthService {

	LoginResult login(LoginRequest request, String userAgent) throws ExecutionException, InterruptedException;

	LoginResult refresh(String refreshTokenValue, String userAgent);

	void logout(String username);

	UserResponse getCurrentUser();
}
