package com.offerpilot.user.controller;

import com.offerpilot.common.result.Result;
import com.offerpilot.common.service.MinioService;
import com.offerpilot.user.vo.FileUploadVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final MinioService minioService;

    /**
     * POST /api/files/upload — 上传文件（头像、简历附件等）
     *
     * @param file   上传的文件（Multipart）
     * @param userId 用户 ID（从请求头 X-User-Id 自动注入）
     * @param type   上传类型：avatar（默认）| resume（简历附件）
     * @return objectName（MinIO 路径）和 url（预签名访问 URL）
     */
    @PostMapping("/upload")
    public Result<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "type", defaultValue = "avatar") String type) {

        if (file.isEmpty()) {
            return Result.badRequest("文件不能为空");
        }

        // 提取扩展名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        // 生成唯一对象名：{type}/{userId}/{uuid}.ext
        String objectName = String.format("%s/%d/%s%s", type, userId, UUID.randomUUID(), ext);

        // 上传到 MinIO
        minioService.upload(objectName, file);
        String url = minioService.getUrl(objectName);

        log.info("文件上传成功: userId={}, objectName={}, size={}, type={}",
                userId, objectName, file.getSize(), file.getContentType());

        return Result.success(new FileUploadVO(objectName, url));
    }

    /**
     * GET /api/files/download?path=avatar/123/uuid.jpg — 下载/预览文件
     * <p>
     * 直接返回文件流，适合 avatar 展示等场景。
     * 路径参数来自 upload 返回的 objectName。
     */
    @GetMapping("/download")
    public void download(@RequestParam String path, HttpServletResponse response) {
        try {
            // 根据扩展名推测 content-type
            String contentType = guessContentType(path);
            response.setContentType(contentType);

            try (InputStream in = minioService.download(path)) {
                in.transferTo(response.getOutputStream());
                response.flushBuffer();
            }
        } catch (Exception e) {
            log.error("文件下载失败: path={}", path, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ==================== 内部辅助 ====================

    /**
     * 根据文件名后缀猜测 MIME 类型
     */
    private String guessContentType(String path) {
        if (path == null) return "application/octet-stream";
        String lower = path.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc") || lower.endsWith(".docx"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "application/octet-stream";
    }
}
