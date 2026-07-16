package com.offerpilot.auth.service.impl;

import com.offerpilot.auth.dto.AuthVO;
import com.offerpilot.auth.dto.LoginRequest;
import com.offerpilot.auth.dto.RegisterRequest;
import com.offerpilot.auth.entity.UserAccount;
import com.offerpilot.auth.mapper.UserAccountMapper;
import com.offerpilot.auth.service.AuthService;
import com.offerpilot.auth.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.offerpilot.common.exception.BusinessException;
import com.offerpilot.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthVO register(RegisterRequest request) {
        String username = request.getUsername().trim();

        // 检查用户名是否已存在
        LambdaQueryWrapper<UserAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAccount::getUsername, username);
        long count = userAccountMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 创建用户账号
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setStatus(1);
        account.setUserId(0L); // 占位，后续用户服务完善后关联
        userAccountMapper.insert(account);

        // 用 account.id 作为 userId（MVP 简化方案）
        account.setUserId(account.getId());
        userAccountMapper.updateById(account);

        // 生成 Token
        String token = jwtUtil.generateToken(account.getUserId(), username);
        log.info("用户注册成功: userId={}, username={}", account.getUserId(), username);

        return AuthVO.builder()
                .userId(account.getUserId())
                .username(username)
                .token(token)
                .expiresIn(jwtUtil.getExpirationMs())
                .build();
    }

    @Override
    public AuthVO login(LoginRequest request) {
        String username = request.getUsername().trim();

        // 查找用户
        LambdaQueryWrapper<UserAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAccount::getUsername, username);
        UserAccount account = userAccountMapper.selectOne(queryWrapper);
        if (account == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查账号状态
        if (account.getStatus() == null || account.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 更新最后登录时间
        account.setLastLoginAt(LocalDateTime.now());
        userAccountMapper.updateById(account);

        // 生成 Token
        String token = jwtUtil.generateToken(account.getUserId(), username);
        log.info("用户登录成功: userId={}, username={}", account.getUserId(), username);

        return AuthVO.builder()
                .userId(account.getUserId())
                .username(username)
                .token(token)
                .expiresIn(jwtUtil.getExpirationMs())
                .build();
    }
}
