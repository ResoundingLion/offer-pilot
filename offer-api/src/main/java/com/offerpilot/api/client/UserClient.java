package com.offerpilot.api.client;

import com.offerpilot.api.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "offer-user", contextId = "userClient", path = "/internal/users")
public interface UserClient {

    /**
     * 创建用户资料（认证服务注册时调用）
     */
    @PostMapping
    UserDTO createUser(@RequestBody UserDTO request);
}
