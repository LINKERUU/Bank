package com.bank.controller;

import com.bank.dto.*;
import com.bank.exception.UnauthorizedException;
import com.bank.model.User;
import com.bank.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody UserLoginDto loginDto,
            HttpSession session
    ) {
        AuthResponseDto response = authService.login(loginDto);

        // сохраняем пользователя в сессию!
        session.setAttribute("user", response.getUser());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody UserRegistrationDto registrationDto) {
        AuthResponseDto response = authService.register(registrationDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot")
    public  ResponseEntity<AuthResponseDto> forgotPassword(@RequestBody UserForgotDto forgotDto) {
        AuthResponseDto response = authService.forgotPassword(forgotDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public  ResponseEntity<AuthResponseDto> forgotPassword(@RequestBody UserResetDto resetDto) {
        AuthResponseDto response = authService.resetPassword(resetDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public UserDto me(HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("user");
        if (user == null) throw new UnauthorizedException("Not logged in");
        return user;
    }


}
