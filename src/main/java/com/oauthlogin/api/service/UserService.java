package com.oauthlogin.api.service;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauthlogin.api.entity.user.User;
import com.oauthlogin.api.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> getUser(String userId) {
        return userRepository.findByUserId(userId);
    }
}
