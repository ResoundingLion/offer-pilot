package com.offerpilot.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "成功"),
    CREATED(201, "创建成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 业务错误码
    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已被注册"),
    USER_NOT_FOUND(1003, "用户不存在"),
    PASSWORD_ERROR(1004, "密码错误"),
    ACCOUNT_LOCKED(1005, "账号已被锁定"),
    TOKEN_EXPIRED(1006, "Token已过期"),
    TOKEN_INVALID(1007, "无效的Token"),
    COMPANY_NOT_FOUND(2001, "公司不存在"),
    POSITION_NOT_FOUND(3001, "岗位不存在"),
    APPLICATION_NOT_FOUND(4001, "投递记录不存在"),
    INVALID_STATUS(4002, "状态流转不合法"),
    INTERVIEW_NOT_FOUND(5001, "面试记录不存在"),
    OFFER_NOT_FOUND(6001, "Offer记录不存在");

    private final int code;
    private final String message;
}
