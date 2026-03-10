package com.george.invetorymanagementsystem.service;

import com.george.invetorymanagementsystem.dto.LoginRequest;
import com.george.invetorymanagementsystem.dto.RegisterRequest;
import com.george.invetorymanagementsystem.dto.Response;
import com.george.invetorymanagementsystem.dto.UserDTO;
import com.george.invetorymanagementsystem.entity.User;

public interface UserService {

    Response registerUser(RegisterRequest registerRequest);

    Response loginUser(LoginRequest loginRequest);

    Response getAllUsers();

    User getCurrentLoggedInUser();

    Response updateUser(Long id, UserDTO userDTO);

    Response deleteUser(Long id);

    Response getUserTransactions(Long id);
}
