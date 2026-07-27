package com.offerpilot.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 文件上传响应
 */
@Data
@AllArgsConstructor
public class FileUploadVO {
    /** MinIO 对象路径，可用于下载和存数据库 */
    private String objectName;
    /** 预签名临时访问 URL */
    private String url;
}
