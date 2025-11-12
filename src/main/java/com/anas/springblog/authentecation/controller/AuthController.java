package com.anas.springblog.authentecation.controller;

import com.anas.springblog.authentecation.dto.AuthRequest;
import com.anas.springblog.authentecation.dto.AuthResponse;
import com.anas.springblog.authentecation.dto.RegistrationRequest;
import com.anas.springblog.authentecation.dto.TokenRequest;
import com.anas.springblog.authentecation.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody RegistrationRequest request) {
        userService.registerUser(request);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = userService.authenticateUser(authRequest);
        return new ResponseEntity<>(authResponse,HttpStatus.OK);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenRequest request) {
        userService.logout(request.refreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRequest request) {
        AuthResponse authResponse = userService.refreshAccessToken(request.refreshToken());
        return new ResponseEntity<>(authResponse,HttpStatus.OK);
    }
}
