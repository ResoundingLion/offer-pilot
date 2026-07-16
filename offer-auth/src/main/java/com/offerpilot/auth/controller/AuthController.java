package com.offerpilot.auth.controller;

import com.offerpilot.auth.dto.AuthVO;
import com.offerpilot.auth.dto.LoginRequest;
import com.offerpilot.auth.dto.RegisterRequest;
import com.offerpilot.auth.service.AuthService;
import com.offerpilot.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<AuthVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.created(authService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }
}
