package com.offerpilot.user.controller;

import com.offerpilot.common.result.Result;
import com.offerpilot.user.converter.UserConverter;
import com.offerpilot.user.dto.ProfileUpdateRequest;
import com.offerpilot.user.dto.UserCreateRequest;
import com.offerpilot.user.dto.UserUpdateRequest;
import com.offerpilot.user.entity.User;
import com.offerpilot.user.service.UserService;
import com.offerpilot.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // ====== 个人中心（当前用户） ======

    /**
     * GET /api/users/me —— 获取当前用户资料
     * 如果 user 记录不存在（老用户或 Feign 异常），自动创建兜底
     */
    @GetMapping("/me")
    public Result<UserVO> getMyProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-Username", required = false) String username) {
        User user = userService.findById(userId);
        if (user == null) {
            // 自动创建 user 记录
            user = new User();
            user.setId(userId);
            user.setName(username != null ? username : "用户" + userId);
            userService.create(user);
            log.info("自动创建用户资料: userId={}, name={}", userId, user.getName());
        }
        return Result.success(UserConverter.convertToVO(user));
    }

    /**
     * PUT /api/users/me —— 更新当前用户资料
     */
    @PutMapping("/me")
    public Result<UserVO> updateMyProfile(
            @RequestBody ProfileUpdateRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-Username", required = false) String username) {
        User existing = userService.findById(userId);
        if (existing == null) {
            // 自动创建 user 记录再更新
            existing = new User();
            existing.setId(userId);
            existing.setName(username != null ? username : "用户" + userId);
            userService.create(existing);
            log.info("自动创建用户资料: userId={}, name={}", userId, existing.getName());
        }

        // 只更新前端传了非空值的字段
        if (request.getName() != null) existing.setName(request.getName());
        if (request.getEmail() != null) existing.setEmail(request.getEmail());
        if (request.getPhone() != null) existing.setPhone(request.getPhone());
        if (request.getAvatar() != null) existing.setAvatar(request.getAvatar());

        User updated = userService.update(existing);
        return Result.success(UserConverter.convertToVO(updated));
    }

    // ====== 通用用户查询 ======

    // 查单个用户
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);

        if(user == null) {
            return Result.notFound();
        }

        return Result.success(UserConverter.convertToVO(user));
    }

    // 查所有用户
    @GetMapping
    public Result<List<UserVO>> getAllUsers() {
        List<User> users = userService.findAll();

        List<UserVO> vos = users.stream()
                .map(UserConverter::convertToVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    // 创建用户
    @PostMapping
    public Result<UserVO> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        User user = UserConverter.convertToEntity(userCreateRequest);
        User userCreated = userService.create(user);
        return Result.created(UserConverter.convertToVO(userCreated));
    }

    // 更新用户
    @PutMapping
    public Result<UserVO> updateUser(@Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        User existing = userService.findById(userUpdateRequest.getId());
        if (existing == null) {
            return Result.notFound();
        }

        User user = UserConverter.convertToEntity(userUpdateRequest);
        User updated = userService.update(user);
        return Result.success(UserConverter.convertToVO(updated));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        // 先查存不存在
        User existing = userService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }
        userService.deleteById(id);
        return Result.success();
    }

}
