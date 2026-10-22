package com.example.userservice.service;

import com.example.userservice.entity.User;

import java.util.List;

public interface UserService {

    User createUser(User user);

    List<User> getAllUsers();

    User getUserById(Long id);
}
