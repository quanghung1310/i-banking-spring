package com.backend.service;

import com.backend.model.Account;

import java.util.List;

public interface IUserService {
    List<Account> getUsers(String logId, int type, long userId);
}
