package com.lms.backend.mapper;

import com.lms.backend.dto.request.AccountRequest;
import com.lms.backend.dto.response.AccountResponse;
import com.lms.backend.entity.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountEntity toEntity(AccountRequest request) {
        if (request == null) return null;

        AccountEntity account = new AccountEntity();
        account.setEmail(request.getEmail());
        account.setPassword(request.getPassword());
        account.setRoleId(request.getRoleId());
        account.setStatus(request.getStatus());
        account.setDeleted(request.getDeleted());
        account.setCreatedBy(request.getCreatedBy());
        account.setUpdateBy(request.getUpdateBy());
        return account;
    }

    public AccountResponse toResponse(AccountEntity entity) {
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
