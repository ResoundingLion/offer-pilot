package com.offerpilot.user.service;

import com.offerpilot.user.entity.User;

import java.util.List;

public interface UserService {
    User findById(Long id);

    List<User> findAll();

    User create(User user);

    User update(User user);

    void deleteById(Long id);
}
