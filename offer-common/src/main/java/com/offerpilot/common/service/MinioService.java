package com.offerpilot.common.service;

import cn.hutool.core.util.StrUtil;
import com.offerpilot.common.config.MinioProperties;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 对象存储服务
 * <p>
 * 封装文件上传、下载、删除、获取 URL 等操作。
 * 所有文件路径格式：{bucketName}/{folder}/{filename}
 * 示例：offerpilot/avatar/123e4567-e89b-12d3-a456-426614174000.jpg
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    // ==================== 桶管理 ====================

    /**
     * 确保默认桶存在，不存在则自动创建
     */
    public void ensureBucketExists() {
        ensureBucketExists(properties.getBucketName());
    }

    /**
     * 确保指定桶存在，不存在则自动创建
     */
    public void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO 桶已自动创建: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查/创建 MinIO 桶失败: {}", bucketName, e);
            throw new RuntimeException("MinIO 桶操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有桶列表
     */
    public List<Bucket> listBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            log.error("获取 MinIO 桶列表失败", e);
            throw new RuntimeException("获取桶列表失败", e);
        }
    }

    // ==================== 上传 ====================

    /**
     * 上传文件到默认桶
     *
     * @param objectName  文件路径（如 avatar/123.jpg）
     * @param inputStream 文件流
     * @param size        文件大小
     * @param contentType 文件 MIME 类型（如 image/jpeg）
     * @return 文件在 MinIO 中的完整对象名
     */
    public String upload(String objectName, InputStream inputStream, long size, String contentType) {
        return upload(properties.getBucketName(), objectName, inputStream, size, contentType);
    }

    /**
     * 上传文件到指定桶
     *
     * @param bucketName  桶名
     * @param objectName  文件路径
     * @param inputStream 文件流
     * @param size        文件大小
     * @param contentType 文件 MIME 类型
     * @return 文件在 MinIO 中的完整对象名
     */
    public String upload(String bucketName, String objectName, InputStream inputStream,
                         long size, String contentType) {
        try {
            // 确保桶存在
            ensureBucketExists(bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build());

            log.info("MinIO 上传成功: {}/{} ({} bytes, {})", bucketName, objectName, size, contentType);
            return objectName;
        } catch (Exception e) {
            log.error("MinIO 上传失败: {}/{}", bucketName, objectName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传 MultipartFile 到默认桶（Spring MVC 场景便捷方法）
     *
     * @param objectName 文件路径
     * @param file       Spring MultipartFile
     * @return 文件在 MinIO 中的完整对象名
     */
    public String upload(String objectName, MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (StrUtil.isBlank(contentType)) {
                contentType = "application/octet-stream";
            }
            return upload(objectName, file.getInputStream(), file.getSize(), contentType);
        } catch (Exception e) {
            log.error("MinIO 上传 MultipartFile 失败: {}", objectName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    // ==================== 下载 ====================

    /**
     * 从默认桶下载文件
     *
     * @param objectName 文件路径
     * @return 文件输入流
     */
    public InputStream download(String objectName) {
        return download(properties.getBucketName(), objectName);
    }

    /**
     * 从指定桶下载文件
     *
     * @param bucketName 桶名
     * @param objectName 文件路径
     * @return 文件输入流
     */
    public InputStream download(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            log.error("MinIO 下载失败: {}/{}", bucketName, objectName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    // ==================== 删除 ====================

    /**
     * 从默认桶删除文件
     *
     * @param objectName 文件路径
     */
    public void delete(String objectName) {
        delete(properties.getBucketName(), objectName);
    }

    /**
     * 从指定桶删除文件
     *
     * @param bucketName 桶名
     * @param objectName 文件路径
     */
    public void delete(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            log.info("MinIO 删除成功: {}/{}", bucketName, objectName);
        } catch (Exception e) {
            log.error("MinIO 删除失败: {}/{}", bucketName, objectName, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    // ==================== 文件信息 ====================

    /**
     * 检查默认桶中文件是否存在
     */
    public boolean exists(String objectName) {
        return exists(properties.getBucketName(), objectName);
    }

    /**
     * 检查指定桶中文件是否存在
     */
    public boolean exists(String bucketName, String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 预签名 URL ====================

    /**
     * 获取默认桶中文件的临时访问 URL（默认 7 天有效）
     *
     * @param objectName 文件路径
     * @return 可浏览器直接访问的 URL
     */
    public String getUrl(String objectName) {
        return getUrl(properties.getBucketName(), objectName, 7, TimeUnit.DAYS);
    }

    /**
     * 获取默认桶中文件的临时访问 URL（自定义过期时间）
     *
     * @param objectName 文件路径
     * @param expiry     过期时长
     * @param unit       时间单位
     * @return 可浏览器直接访问的 URL
     */
    public String getUrl(String objectName, int expiry, TimeUnit unit) {
        return getUrl(properties.getBucketName(), objectName, expiry, unit);
    }

    /**
     * 获取指定桶中文件的临时访问 URL
     *
     * @param bucketName 桶名
     * @param objectName 文件路径
     * @param expiry     过期时长
     * @param unit       时间单位
     * @return 可浏览器直接访问的 URL
     */
    public String getUrl(String bucketName, String objectName, int expiry, TimeUnit unit) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry, unit)
                            .build());
        } catch (Exception e) {
            log.error("MinIO 获取 URL 失败: {}/{}", bucketName, objectName, e);
            return null;
        }
    }
}
