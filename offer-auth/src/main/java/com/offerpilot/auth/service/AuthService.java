package com.offerpilot.auth.service;

import com.offerpilot.auth.dto.AuthVO;
import com.offerpilot.auth.dto.LoginRequest;
import com.offerpilot.auth.dto.RegisterRequest;

public interface AuthService {

    /**
     * 用户注册
     */
    AuthVO register(RegisterRequest request);

    /**
     * 用户登录
     */
    AuthVO login(LoginRequest request);
}
