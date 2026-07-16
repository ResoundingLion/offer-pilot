package com.offerpilot.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotNull
    private Long id;

    @NotBlank
    @Email
    private String email;

    private String phone;

    @NotBlank
    private String name;

    private String avatar;

}
