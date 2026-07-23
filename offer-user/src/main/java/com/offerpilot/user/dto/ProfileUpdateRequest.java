package com.offerpilot.user.dto;

import lombok.Data;

/**
 * 用户资料更新（/api/users/me 专用）
 * 不传 id —— 从 X-User-Id 请求头取
 */
@Data
public class ProfileUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private String avatar;
}
