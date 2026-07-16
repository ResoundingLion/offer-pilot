package com.offerpilot.common.constant;

/**
 * 通用常量
 */
public interface CommonConstant {

    /** 日期格式 */
    String DATE_FORMAT = "yyyy-MM-dd";
    String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** 时区 */
    String TIME_ZONE = "Asia/Shanghai";

    /** 默认页码 */
    int DEFAULT_PAGE = 1;
    int DEFAULT_PAGE_SIZE = 20;

    /** 字符编码 */
    String CHARSET_UTF_8 = "UTF-8";

    /** HTTP 请求头 */
    String HEADER_AUTHORIZATION = "Authorization";
    String HEADER_AUTH_PREFIX = "Bearer ";
}
