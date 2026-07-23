package com.offerpilot.user.controller.internal;

import com.offerpilot.api.dto.UserDTO;
import com.offerpilot.user.entity.User;
import com.offerpilot.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部接口 —— 供 auth 服务通过 Feign 调用创建用户资料
 * 不经过网关、不走 Result 包装
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    @PostMapping
    public UserDTO createUser(@RequestBody UserDTO dto) {
        User user = new User();
        user.setName(dto.getName() != null ? dto.getName() : "用户" + System.currentTimeMillis());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        userService.create(user);
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone());
    }
}
