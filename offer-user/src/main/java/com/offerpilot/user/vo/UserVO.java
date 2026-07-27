package com.offerpilot.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String email;
    private String phone;
    private String avatar;
    /** 头像临时访问 URL（由后端根据 avatar 路径实时生成） */
    private String avatarUrl;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
