package com.offerpilot.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank
    @Email
    private String email;

    private String phone;

    @NotBlank
    private String name;

    private String avatar;
}
