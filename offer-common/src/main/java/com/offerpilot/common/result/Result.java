package com.offerpilot.common.result;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private Long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // ---- Success ----

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> created() {
        return new Result<>(ResultCode.CREATED.getCode(), ResultCode.CREATED.getMessage(), null);
    }

    public static <T> Result<T> created(T data) {
        return new Result<>(ResultCode.CREATED.getCode(), ResultCode.CREATED.getMessage(), data);
    }

    // ---- Error ----

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    // ---- Convenience helpers ----

    public static <T> Result<T> badRequest() {
        return error(ResultCode.BAD_REQUEST);
    }

    public static <T> Result<T> badRequest(String message) {
        return error(ResultCode.BAD_REQUEST, message);
    }

    public static <T> Result<T> unauthorized() {
        return error(ResultCode.UNAUTHORIZED);
    }

    public static <T> Result<T> unauthorized(String message) {
        return error(ResultCode.UNAUTHORIZED, message);
    }

    public static <T> Result<T> notFound() {
        return error(ResultCode.NOT_FOUND);
    }

    public static <T> Result<T> notFound(String message) {
        return error(ResultCode.NOT_FOUND, message);
    }

    public static <T> Result<T> internalError() {
        return error(ResultCode.INTERNAL_ERROR);
    }

    public static <T> Result<T> internalError(String message) {
        return error(ResultCode.INTERNAL_ERROR, message);
    }

    public static <T> Result<T> forbidden() {
        return error(ResultCode.FORBIDDEN);
    }

    public static <T> Result<T> forbidden(String message) {
        return error(ResultCode.FORBIDDEN, message);
    }
}
