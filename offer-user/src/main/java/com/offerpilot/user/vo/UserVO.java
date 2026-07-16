package com.offerpilot.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String email;
    private String phone;
    private String avatar;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
