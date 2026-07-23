package com.offerpilot.auth.service;

import com.offerpilot.api.client.UserClient;
import com.offerpilot.api.dto.UserDTO;
import com.offerpilot.auth.dto.AuthVO;
import com.offerpilot.auth.dto.LoginRequest;
import com.offerpilot.auth.dto.RegisterRequest;
import com.offerpilot.auth.entity.UserAccount;
import com.offerpilot.auth.mapper.UserAccountMapper;
import com.offerpilot.auth.service.impl.AuthServiceImpl;
import com.offerpilot.auth.util.JwtUtil;
import com.offerpilot.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl 单元测试
 *
 * 覆盖：注册（成功/重复用户名）、登录（成功/账号锁定/密码错误/用户不存在）
 * 注：register 中 UserClient Feign 调用、login 中 JWT 生成，均通过 Mock 隔离
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserAccountMapper userAccountMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserClient userClient;

    private AuthServiceImpl authService;

    private final String username = "testuser";
    private final String password = "password123";
    private final String encodedPassword = "encoded_password";
    private final String token = "jwt_token_xxx";

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userAccountMapper, passwordEncoder, jwtUtil, userClient);
    }

    // ========================================================================
    // 注册
    // ========================================================================

    @Nested
    @DisplayName("注册")
    class Register {

        @Test
        @DisplayName("注册成功 → 创建用户资料 + 账号 + 返回 Token")
        void registerSuccess() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(username);
            request.setPassword(password);

            // 用户名未占用
            when(userAccountMapper.selectCount(any())).thenReturn(0L);
            // Feign 创建用户资料成功
            when(userClient.createUser(any())).thenReturn(new UserDTO(1L, username, null, null));
            // 密码加密
            when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
            // Token 生成
            when(jwtUtil.generateToken(1L, username)).thenReturn(token);
            when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

            AuthVO result = authService.register(request);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo(username);
            assertThat(result.getToken()).isEqualTo(token);
            assertThat(result.getExpiresIn()).isEqualTo(86400000L);

            // 验证调用了 insert
            verify(userAccountMapper).insert(Mockito.<UserAccount>any());
        }

        @Test
        @DisplayName("注册时用户名已存在 → 抛 BusinessException")
        void registerDuplicateUsername() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(username);
            request.setPassword(password);

            when(userAccountMapper.selectCount(any())).thenReturn(1L);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户名已存在");

            // 用户名已存在 → 不应调用 Feign 创建用户，也不应 insert
            verify(userClient, never()).createUser(any());
            verify(userAccountMapper, never()).insert(Mockito.<UserAccount>any());
        }
    }

    // ========================================================================
    // 登录
    // ========================================================================

    @Nested
    @DisplayName("登录")
    class Login {

        private UserAccount createAccount(Long id, String username, String password, Integer status) {
            UserAccount account = new UserAccount();
            account.setId(id);
            account.setUserId(id);
            account.setUsername(username);
            account.setPassword(password);
            account.setStatus(status);
            return account;
        }

        @Test
        @DisplayName("登录成功 → 更新 lastLoginAt + 返回 Token")
        void loginSuccess() {
            LoginRequest request = new LoginRequest();
            request.setUsername(username);
            request.setPassword(password);

            UserAccount account = createAccount(1L, username, encodedPassword, 1);
            when(userAccountMapper.selectOne(any())).thenReturn(account);
            when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
            when(jwtUtil.generateToken(1L, username)).thenReturn(token);
            when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

            AuthVO result = authService.login(request);

            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo(username);
            assertThat(result.getToken()).isEqualTo(token);

            // 验证更新了 lastLoginAt
            verify(userAccountMapper).updateById(Mockito.<UserAccount>any());
        }

        @Test
        @DisplayName("登录时用户不存在 → 抛 BusinessException")
        void loginUserNotFound() {
            LoginRequest request = new LoginRequest();
            request.setUsername("nonexistent");
            request.setPassword(password);

            when(userAccountMapper.selectOne(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("登录时账号已禁用 → 抛 BusinessException")
        void loginAccountLocked() {
            LoginRequest request = new LoginRequest();
            request.setUsername(username);
            request.setPassword(password);

            UserAccount account = createAccount(1L, username, encodedPassword, 0);
            when(userAccountMapper.selectOne(any())).thenReturn(account);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("账号已被锁定");
        }

        @Test
        @DisplayName("登录时密码错误 → 抛 BusinessException")
        void loginWrongPassword() {
            LoginRequest request = new LoginRequest();
            request.setUsername(username);
            request.setPassword("wrong_password");

            UserAccount account = createAccount(1L, username, encodedPassword, 1);
            when(userAccountMapper.selectOne(any())).thenReturn(account);
            when(passwordEncoder.matches("wrong_password", encodedPassword)).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("密码错误");
        }
    }
}
