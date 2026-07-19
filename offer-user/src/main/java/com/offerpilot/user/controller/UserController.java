package com.offerpilot.user.controller;

import com.offerpilot.common.result.Result;
import com.offerpilot.user.converter.UserConverter;
import com.offerpilot.user.dto.UserCreateRequest;
import com.offerpilot.user.dto.UserUpdateRequest;
import com.offerpilot.user.entity.User;
import com.offerpilot.user.service.UserService;
import com.offerpilot.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
