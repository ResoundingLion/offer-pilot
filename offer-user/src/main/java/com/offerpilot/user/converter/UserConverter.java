package com.offerpilot.user.converter;

import com.offerpilot.user.dto.UserCreateRequest;
import com.offerpilot.user.dto.UserUpdateRequest;
import com.offerpilot.user.entity.User;
import com.offerpilot.user.vo.UserVO;

public class UserConverter {

    public static UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setName(user.getName());
        vo.setAvatar(user.getAvatar());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    public static User convertToEntity(UserCreateRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setName(request.getName());
        user.setAvatar(request.getAvatar());
        return user;
    }

    public static User convertToEntity(UserUpdateRequest request) {
        User user = new User();
        user.setId(request.getId());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setName(request.getName());
        user.setAvatar(request.getAvatar());
        return user;
    }
}
