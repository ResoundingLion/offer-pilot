package com.offerpilot.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 对象存储配置
 * 从 application.yml / Nacos 读取 minio 前缀的属性
 * <p>
 * 使用方式：在 application.yml 中配置
 * <pre>{@code
 * minio:
 *   endpoint: http://localhost:9000
 *   access-key: offerpilot
 *   secret-key: offerpilot123
 *   bucket-name: offerpilot
 * }</pre>
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO 服务端点（容器内访问用容器名 + 端口 9000）
     * 宿主机访问用 http://localhost:9000
     */
    private String endpoint = "http://minio:9000";

    /**
     * 访问密钥（对应控制台的 Access Key）
     */
    private String accessKey = "offerpilot";

    /**
     * 秘密密钥（对应控制台的 Secret Key）
     */
    private String secretKey = "offerpilot123";

    /**
     * 默认存储桶名称
     */
    private String bucketName = "offerpilot";
}
