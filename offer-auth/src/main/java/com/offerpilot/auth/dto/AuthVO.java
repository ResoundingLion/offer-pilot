package com.offerpilot.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthVO {

    private Long userId;
    private String username;
    private String token;
    private Long expiresIn;
}
