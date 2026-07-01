package com.lms.backend.service.impl;

import com.google.cloud.firestore.*;
import com.lms.backend.dto.request.AccountRequest;
import com.lms.backend.dto.response.AccountResponse;
import com.lms.backend.entity.AccountEntity;
import com.lms.backend.mapper.AccountMapper;
import com.lms.backend.repository.AccountRepository;
import com.lms.backend.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class AccountServiceImpl implements IAccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public String createAccount(AccountRequest request) throws Exception {

        AccountEntity account = accountMapper.toEntity(request);

        account.setCreateDate(LocalDateTime.now());
        account.setCreatedBy(request.getRequestedBy());
        account.setDeleted(false);

        if (account.getStatus() == null) {
            account.setStatus("ACTIVE");
        }

        return accountRepository.save(account);
    }

    @Override
    public List<AccountResponse> getAllAccounts() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs =
                accountRepository.findAllActive();

        return docs.stream()
                .map(doc -> {
                    AccountEntity account = doc.toObject(AccountEntity.class);
                    account.setId(doc.getId());
                    return accountMapper.toResponse(account);
                })
                .toList();

    }

    @Override
    public AccountResponse getAccountById(String accountId) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot =
                accountRepository.findById(accountId);

        if (!snapshot.exists()) {
            throw new IllegalArgumentException("Tài khoản với ID " + accountId + " không tồn tại!");
        }

        AccountEntity account = snapshot.toObject(AccountEntity.class);

        if (account == null || Boolean.TRUE.equals(account.getDeleted())) {
            throw new IllegalArgumentException("Tài khoản với ID " + accountId + " đã bị xóa mềm!");
        }

        account.setId(snapshot.getId());

        return accountMapper.toResponse(account);
    }

    @Override
    public String updateAccount(String accountId, AccountRequest request)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot snapshot = accountRepository.findById(accountId);

        if (!snapshot.exists()) {
            throw new IllegalArgumentException("Tài khoản với ID " + accountId + " không tồn tại!");
        }

        AccountEntity account = snapshot.toObject(AccountEntity.class);
        if (account == null || Boolean.TRUE.equals(account.getDeleted())) {
            throw new IllegalArgumentException("Tài khoản với ID " + accountId + " đã bị xóa!");
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", request.getEmail());
        updates.put("password", request.getPassword());
        updates.put("roleId", request.getRoleId());
        updates.put("status", request.getStatus());
        updates.put("updateDate", Instant.now());
        updates.put("updateBy", request.getRequestedBy());

        DocumentReference docRef = accountRepository.getAccountCollection().document(accountId);
        accountRepository.update(docRef.getId(), updates);
        return "Cập nhật thành công tài khoản: " + accountId;
    }

    @Override
    public String deleteAccount(String accountId, String deletedBy)
        throws ExecutionException, InterruptedException {

        DocumentSnapshot snapshot = accountRepository.findById(accountId);
        AccountEntity account = snapshot.toObject(AccountEntity.class);


        if (account == null) {
            throw new IllegalArgumentException("Không tồn tại!");
        }
        accountRepository.softDelete(accountId, deletedBy);
        return "Đã xóa mềm tài khoản thành công!";
    }

}
