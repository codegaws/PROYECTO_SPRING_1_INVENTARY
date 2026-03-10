package com.george.invetorymanagementsystem.service.impl;


import com.george.invetorymanagementsystem.dto.LoginRequest;
import com.george.invetorymanagementsystem.dto.RegisterRequest;
import com.george.invetorymanagementsystem.dto.Response;
import com.george.invetorymanagementsystem.dto.UserDTO;
import com.george.invetorymanagementsystem.entity.User;
import com.george.invetorymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Override
    public Response registerUser(RegisterRequest registerRequest) {
        return null;
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public Response getAllUsers() {
        return null;
    }

    @Override
    public User getCurrentLoggedInUser() {
        return null;
    }

    @Override
    public Response updateUser(Long id, UserDTO userDTO) {
        return null;
    }

    @Override
    public Response deleteUser(Long id) {
        return null;
    }

    @Override
    public Response getUserTransactions(Long id) {
        return null;
    }
}
