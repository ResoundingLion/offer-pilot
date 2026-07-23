package com.offerpilot.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户基础信息 DTO —— 供 Feign 跨服务传输
 * 注册时 auth → user 传递 name；创建后返回完整信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
}
