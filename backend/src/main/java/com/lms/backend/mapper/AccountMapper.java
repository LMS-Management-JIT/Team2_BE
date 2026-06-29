package com.lms.backend.mapper;

import com.lms.backend.dto.request.AccountRequest;
import com.lms.backend.dto.response.AccountResponse;
import com.lms.backend.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toEntity(AccountRequest request) {
        if (request == null) return null;

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPassword(request.getPassword());
        account.setRoleId(request.getRoleId());
        account.setStatus(request.getStatus());
        return account;
    }

    public AccountResponse toResponse(Account entity) {
        if (entity == null) return null;

        AccountResponse response = new AccountResponse();
        response.setId(entity.getId());
        response.setEmail(entity.getEmail());
        response.setRoleId(entity.getRoleId());
        response.setStatus(entity.getStatus());
        response.setDeleted(entity.getDeleted());
        response.setCreateDate(entity.getCreateDate());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdateDate(entity.getUpdateDate());
        response.setUpdateBy(entity.getUpdateBy());
        return response;
    }
}

