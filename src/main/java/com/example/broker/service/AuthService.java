package com.example.broker.service;

import com.example.broker.converter.UserConverter;
import com.example.broker.dto.UserDto;
import com.example.broker.entity.Customer;
import com.example.broker.entity.Role;
import com.example.broker.entity.User;
import com.example.broker.exception.CustomException;
import com.example.broker.exception.ErrorType;
import com.example.broker.model.LoginResponse;
import com.example.broker.model.RegisterRequest;
import com.example.broker.model.RegisterResponseModel;
import com.example.broker.repository.CustomerRepository;
import com.example.broker.repository.RoleRepository;
import com.example.broker.repository.UserRepository;
import com.example.broker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponseModel register(RegisterRequest registerRequest) {
        Role role = roleRepository.findByName(registerRequest.getRole())
                .orElseThrow(() -> new CustomException(ErrorType.ROLE_NOT_FOUND));

        Customer customer = null;
        if (registerRequest.getCustomerId() != null) {
            customer = customerRepository.findById(registerRequest.getCustomerId())
                    .orElseThrow(() -> new CustomException(ErrorType.CUSTOMER_NOT_FOUND));
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .customer(customer)
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        UserDto userDto = UserConverter.toDto(savedUser);

        return new RegisterResponseModel(true, userDto.getUsername(), user.getRole().getName());
    }

    public LoginResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRole());
        if (user.getCustomer() != null ){
            claims.put("customerId", user.getCustomer().getId()); // opsiyonel
        }

        String token = jwtService.generateToken(username, claims);
        Date expiresAt = jwtService.getExpirationFromToken(token);
        return new LoginResponse(token, user.getUsername(), user.getRole().getName(), expiresAt);

    }
}
