package com.lms.backend.service;

import com.lms.backend.dto.request.AccountRequest;
import com.lms.backend.dto.response.AccountResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IAccountService {

    String createAccount(AccountRequest request) throws Exception;

    List<AccountResponse> getAllAccounts() throws ExecutionException, InterruptedException;

    AccountResponse getAccountById(String accountId) throws ExecutionException, InterruptedException;

    String updateAccount(String accountId, AccountRequest request) throws ExecutionException, InterruptedException;

    String deleteAccount(String accountId, String deletedBy) throws ExecutionException, InterruptedException;
}

